package com.selfxdsd.core;

import com.selfxdsd.api.Comment;
import javax.json.Json;
import javax.json.JsonObject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Unit tests for {@link GitlabIssueComment}.
 *
 * @version $Id$
 * @since 0.0.45
 */
public final class GitlabIssueCommentTestCase {

    /**
     * Extracts author, id and comment
     * from JsonObject.
     */
    @Test
    public void returnsAuthor() {
        MatcherAssert.assertThat(
            new GitlabIssueComment(
                Json.createObjectBuilder()
                    .add("id", 1)
                    .add(
                        "author",
                        Json.createObjectBuilder()
                            .add("username", "andreoss")
                    )
                    .add("body", "A comment")
                    .build()
            ),
            new IsComment(
                Matchers.is("1"),
                Matchers.is("andreoss"),
                Matchers.is("A comment")
            )
        );
    }

    /**
     * Returns original JsonObject.
     */
    @Test
    public void returnsAsJsonFormat() {
        JsonObject json = Json.createObjectBuilder()
            .add("id", 1)
            .add("body", "This is a comment")
            .build();
        final Comment comment = new GitlabIssueComment(json);
        MatcherAssert.assertThat(
            comment.json(),
            Matchers.is(json)
        );
    }
}
