package com.selfxdsd.core;

import com.selfxdsd.api.Organization;
import com.selfxdsd.api.User;
import com.selfxdsd.api.storage.Storage;
import com.selfxdsd.core.mock.MockJsonResources;
import com.selfxdsd.core.mock.MockJsonResources.MockResource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import javax.json.Json;
import javax.json.JsonValue;
import java.net.URI;

/**
 * Unit tests for {@link BitbucketOrganizations}.
 *
 * @author Nikita Monokov(nmonokov@gmail.com)
 * @version $Id$
 * @since 0.0.62
 */
public final class BitbucketOrganizationsTestCase {

    /**
     * Rule to verify a specific exception with a specific message.
     */
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    /**
     * Fetches organizations.
     */
    @Test
    public void fetchesOk() {
        final JsonResources resources = new MockJsonResources(
            new AccessToken.Bitbucket("bitbucket123"),
            req -> new MockResource(200, Json
                .createArrayBuilder()
                .add(Json
                    .createObjectBuilder()
                    .add("uuid", "{123}")
                    .build())
                .add(Json
                    .createObjectBuilder()
                    .add("uuid", "{456}")
                    .build())
                .build())
        );

        final Iterable<Organization> organizations =
            () -> new BitbucketOrganizations(resources,
                URI.create("https://bitbucket.org/api/2.0/"),
                Mockito.mock(User.class),
                Mockito.mock(Storage.class)).iterator();
        MatcherAssert.assertThat(organizations, Matchers.iterableWithSize(2));
    }

    /**
     * Throws if current User is not authenticated.
     */
    @Test
    public void throwsWhenNotAuthenticated(){
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Current User is "
            + "not authenticated.");

        final JsonResources resources = new MockJsonResources(
            null,
            req -> new MockResource(401, JsonValue.NULL));
        new BitbucketOrganizations(resources,
            URI.create("https://bitbucket.org/api/2.0/"),
            Mockito.mock(User.class),
            Mockito.mock(Storage.class)).iterator();
    }

    /**
     * Throws when organizations are not fetched.
     */
    @Test
    public void throwsWhenUnableToFetch() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Unable to fetch Bitbucket "
            + "organizations for current User. Expected 200 OK, "
            + "but got: 500");

        final JsonResources resources = new MockJsonResources(
            null,
            req -> new MockResource(500, JsonValue.NULL));
        new BitbucketOrganizations(resources,
            URI.create("https://bitbucket.org/api/2.0/"),
            Mockito.mock(User.class),
            Mockito.mock(Storage.class)).iterator();
    }
}
