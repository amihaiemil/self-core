/**
 * Copyright (c) 2020, Self XDSD Contributors
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to read the Software only. Permission is hereby NOT GRANTED to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software.
 *
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
package com.selfxdsd.api;

/**
 * A Project is a User's Repository which has been
 * registered (activated) on the Self platform.
 *
 * Once activated, a project will be managed by one of
 * Self's Project Managers.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.0.1
 */
public interface Project {

    /**
     * Full name of the Repo represented by this Project.
     * @return String.
     */
    String repoFullName();

    /**
     * Provider (github, gitlab etc).
     * @return String.
     */
    String provider();

    /**
     * Who owns this project?
     * @return User.
     */
    User owner();

    /**
     * Project Manager in charge of this Project.
     * @return ProjectManager.
     */
    ProjectManager projectManager();

    /**
     * This project's repository.
     * @return Repo.
     */
    Repo repo();

    /**
     * This project's Contributor Contracts.
     * @return Contracts.
     */
    Contracts contracts();

    /**
     * This project's Contributors.
     * @return Contributors.
     */
    Contributors contributors();

    /**
     * Active tasks in this project.
     * @return Tasks.
     */
    Tasks tasks();

    /**
     * Language spoken in this Project.
     * @return Language.
     */
    Language language();

    /**
     * Resolve the given event that happened in this Project.
     * @param event Event received from the Provider.
     * @param secret Secret token to make sure the event actually
     *  belongs to this Project.
     * @throws IllegalStateException If the provided secret
     *  is not correct.
     */
    void resolve(final Event event, final String secret);

    /**
     * This project's webhook token.
     * @return Secret token to be specified in the webhook.
     */
    String webHookToken();

    /**
     * Deactivate this project, tell  Self to stop
     * managing it.
     * @return This project's repository.
     */
    Repo deactivate();
}
