/**
 * Copyright (c) 2020, Self XDSD Contributors
 * All rights reserved.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to read the Software only. Permission is hereby NOT GRANTED to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.selfxdsd.core.projects;

import com.selfxdsd.api.*;
import com.selfxdsd.api.exceptions.WalletAlreadyExistsException;
import com.selfxdsd.api.storage.Storage;
import com.selfxdsd.core.Env;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A Project stored in Self. Use this class whe implementing the storage.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.0.1
 * @todo #31:30min Implement the deactivate method which should remove the
 *  Project form the DB (it means Self will stop managing it). Return the
 *  corresponding Repo when done. Don't forget the tests.
 * @todo #278:30min Continue implementation of the resolve(...) method.
 *  It should decide what kind of event has occurred and delegate it
 *  further to the ProjectManager who will deal with it. We still need
 *  the Issue Assigned case and Comment Created case.
 */
public final class StoredProject implements Project {

    /**
     * Owner of this Project.
     */
    private final User owner;

    /**
     * Repo full name.
     */
    private final String repoFullName;

    /**
     * WebHook token.
     */
    private String webHookToken;

    /**
     * Manager in charge of this Project.
     */
    private final ProjectManager projectManager;

    /**
     * Self's Storage.
     */
    private final Storage storage;

    /**
     * Constructor.
     * @param owner Owner of the project/repo.
     * @param repoFullName Repo full name.
     * @param webHookToken Webhook token.
     * @param projectManager Manager in charge.
     * @param storage Storage of Self.
     * @checkstyle ParameterNumber (10 lines)
     */
    public StoredProject(
        final User owner,
        final String repoFullName,
        final String webHookToken,
        final ProjectManager projectManager,
        final Storage storage
    ) {
        this.owner = owner;
        this.repoFullName = repoFullName;
        this.webHookToken = webHookToken;
        this.projectManager = projectManager;
        this.storage = storage;
    }

    @Override
    public String repoFullName() {
        return this.repoFullName;
    }

    @Override
    public String provider() {
        return this.owner.provider().name();
    }

    @Override
    public User owner() {
        return this.owner;
    }

    @Override
    public Wallets wallets() {
        return this.storage.wallets().ofProject(this);
    }

    @Override
    public Wallet wallet() {
        return this.wallets().active();
    }

    @Override
    public ProjectManager projectManager() {
        return this.projectManager;
    }

    @Override
    public Repo repo() {
        return this.owner.provider().repo(
            this.repoFullName.substring(0, this.repoFullName.indexOf("/")),
            this.repoFullName.substring(this.repoFullName.indexOf("/") + 1)
        );
    }

    @Override
    public Contracts contracts() {
        return this.storage.contracts()
            .ofProject(this.repoFullName(), this.provider());
    }

    @Override
    public Contributors contributors() {
        return this.storage.contributors().ofProject(
            this.repoFullName(), this.provider()
        );
    }

    @Override
    public Tasks tasks() {
        return this.storage.tasks().ofProject(
            this.repoFullName(),
            this.provider()
        );
    }

    @Override
    public Language language() {
        return new English();
    }

    @Override
    public void resolve(final Event event) {
        final String type = event.type();
        switch (type) {
            case Event.Type.ACTIVATE:
                this.projectManager.newProject(event);
                break;
            case Event.Type.NEW_ISSUE:
                this.projectManager.newIssue(event);
                break;
            case Event.Type.REOPENED_ISSUE:
                this.projectManager.reopenedIssue(event);
                break;
            case Event.Type.UNASSIGNED_TASKS:
                this.projectManager.unassignedTasks(event);
                break;
            case Event.Type.ASSIGNED_TASKS:
                this.projectManager.assignedTasks(event);
                break;
            case Event.Type.ISSUE_COMMENT:
                this.projectManager.comment(event);
                break;
            default:
                break;
        }
    }

    @Override
    public String webHookToken() {
        return this.webHookToken;
    }

    @Override
    public Repo deactivate() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Wallet createStripeWallet() {
        final Wallets wallets = this.wallets();
        for(final Wallet wallet : wallets) {
            if(wallet.type().equalsIgnoreCase(Wallet.Type.STRIPE)) {
                throw new WalletAlreadyExistsException(
                    this, Wallet.Type.STRIPE
                );
            }
        }
        final String apiToken = System.getenv(Env.STRIPE_API_TOKEN);
        if(apiToken == null || apiToken.trim().isEmpty()) {
            throw new IllegalStateException(
                "Please specify the "
                + Env.STRIPE_API_TOKEN
                + " Environment Variable!"
            );
        }
        Stripe.apiKey = apiToken;
        try {
            final String email;
            if(this.owner.email() != null) {
                email = this.owner.email();
            } else {
                email = "";
            }
            final Customer customer = Customer.create(
                CustomerCreateParams.builder()
                    .setName(this.owner.username())
                    .setEmail(email)
                    .setDescription(
                        this.repoFullName + " at " + this.provider()
                    )
                    .build()
            );
            return this.storage.wallets().register(
                this, Wallet.Type.STRIPE,
                BigDecimal.valueOf(0), customer.getId()
            );
        } catch (final StripeException ex) {
            throw new IllegalStateException(
                "Stripe threw an exception when trying to create a Wallet "
                + "(Customer) for Project " + this.repoFullName + " at "
                + this.provider() + ". ",
                ex
            );
        }
    }

    @Override
    public Storage storage() {
        return this.storage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.repoFullName, this.provider());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Project)) {
            return false;
        }
        final Project other = (Project) obj;
        return this.repoFullName.equals(other.repoFullName())
            && this.provider().equals(other.provider());
    }
}
