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
package com.selfxdsd.core;

import com.selfxdsd.api.Project;
import com.selfxdsd.api.Webhook;
import com.selfxdsd.api.Webhooks;
import com.selfxdsd.api.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Iterator;

/**
 * Gitlab repo webhooks.
 *
 * @author criske
 * @version $Id$
 * @since 0.0.13
 * @todo #681:60min Implemented method remove() from this class, which
 *  should remove any webhooks related to Self XDSD from the Gitlab repo.
 */
final class GitlabWebhooks implements Webhooks {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(
        GitlabWebhooks.class
    );

    /**
     * Gitlab repo Webhooks base uri.
     */
    private final URI hooksUri;

    /**
     * Gitlab's JSON Resources.
     */
    private final JsonResources resources;

    /**
     * Self storage, in case we want to store something.
     */
    private final Storage storage;

    /**
     * Ctor.
     *
     * @param resources Gitlab's JSON Resources.
     * @param hooksUri Hooks base URI.
     * @param storage Storage.
     */
    GitlabWebhooks(final JsonResources resources,
                   final URI hooksUri,
                   final Storage storage) {
        this.resources = resources;
        this.hooksUri = hooksUri;
        this.storage = storage;
    }

    /**
     * {@inheritDoc}
     * <br/>
     * More about Gitlab project webhook configuration
     * <a href="https://docs.gitlab.com/ee/api/projects.html#add-project-hook">
     * here</a>.
     */
    @Override
    public boolean add(final Project project) {
        LOG.debug("Adding Gitlab webhook for Project "
            + project.repoFullName());
        final boolean added;
        final Resource response = this.resources.post(
            this.hooksUri,
            Json.createObjectBuilder()
                .add("id", project.repoFullName()
                    .replace("/", "%2F"))
                .add("url", System.getenv(Env.WEBHOOK_BASE_URL) + "/gitlab/"
                    + project.repoFullName())
                .add("issues_events", true)
                .add("token", project.webHookToken())
                .build()
        );
        if (response.statusCode() == HttpURLConnection.HTTP_CREATED) {
            added = true;
            LOG.debug("Webhook added successfully!");
        } else {
            added = false;
            LOG.debug("Problem when adding webhook. Expected 201 CREATED, "
                + " but got " + response.statusCode());
        }
        return added;
    }

    @Override
    public boolean remove() {
        throw new UnsupportedOperationException(
            "Not yet implemented."
        );
    }

    @Override
    public Iterator<Webhook> iterator() {
        throw new UnsupportedOperationException(
            "Not yet implemented."
        );
    }
}
