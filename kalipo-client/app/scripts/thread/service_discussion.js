'use strict';

kalipoApp.factory('Discussion', function (Thread) {

    var internal = {

        sort: function (comments) {
            return _.sortBy(comments, function (comment) {
                return -1 * comment.$score
            })
        },

        postFetch: function (comments, currentPage) {

            return _.forEach(comments, function (comment, index) {
                comment.replies = {
                    $all: [],
                    verbose: [],
                    dropped: []
                };

                comment.$index = currentPage * 200 + index;
                comment.$commentCount = 1;

                if (comment.hidden) {
                    comment.text = 'Content hidden';
                    comment.dislikes = 0;
                    comment.likes = 0;
                }

                if (comment.status == 'DELETED') {
                    comment.displayName = 'Deleted';
                    comment.text = 'Content deleted';
                    comment.dislikes = 0;
                    comment.likes = 0;
                }

                if (_.isUndefined(comment.likes)) {
                    comment.likes = 0;
                }
                if (_.isUndefined(comment.dislikes)) {
                    comment.dislikes = 0;
                }

                comment.$pending = comment.status == 'PENDING';

                // todo minimize negative-only comments, hell-banned subthreads

                comment.$hiddenreplies = comment.dislikes > 3 && comment.dislikes > comment.likes;
                comment.$score = comment.influence / comment.createdDate;

                // author chose to hide his name
                if (_.isEmpty(comment.displayName) || _.isUndefined(comment.displayName)) {
                    comment.displayName = 'Anonymous';
                }

                var total = comment.likes + comment.dislikes;
                comment.$likes = comment.likes / total * 100;
                comment.$dislikes = comment.dislikes / total * 100;

            });
        },

        shape: function (comments, attributes) {
            console.log('shape');
            internal.shapeRc(comments, 1, attributes);
        },

        shapeRc: function (comments, level, attributes) {

            _.forEach(comments, function (comment) {

                /*
                 3 rule sets
                 .) huge discussions > 200 comments attributes.totalElementCount
                 - only level 0 comments
                 - level 1: show best 2, rest is hidden
                 - drop bad comments

                 .) normal discussions
                 - hide index > 4
                 - level 0 are not hiddenreplies

                 .) general rules
                 - minimize bad comments
                 - show full comment if user is the author
                 - show path to authors comment at least onelined

                 */

                comment.$repliesCount = 0;
                var replies = comment.replies.$all;
                var verbose = comment.replies.verbose;
                var dropped = comment.replies.dropped;

                internal.shapeRc(replies, level + 1);

                _.forEach(replies, function (reply, index) {

                    // get reply count
                    comment.$repliesCount += 1; // reply itself
                    comment.$repliesCount += reply.$repliesCount; // its replies

                    var isHidden = index >= 1 && reply.$repliesCount == 0 && reply.$oneline;

                    // todo && older than n views && not owner of comment
                    if (isHidden) {
                        console.log('dropping', reply.id);
                        dropped.push(reply.id);
                    } else {
                        verbose.push(reply);
                    }
                });

                // todo can still be controversial
                comment.$oneline = (comment.likes > 1 || comment.dislikes > 1) && (comment.likes - comment.dislikes) < -1;

                comment.replies.verbose = internal.sort(comment.replies.verbose);

                //delete comment.replies.$all;

            });
        },

        merge: function (tree, comments) {

            var roots = [];

            _.forEach(comments, function (comment) {

                tree[comment.id] = comment;

                if (comment.level == 0 || _.isUndefined(comment.parentId)) {
                    roots.push(comment);

                } else {

                    var parent = tree[comment.parentId];
                    if (_.isUndefined(parent)) {
                        console.log('cannot find parent of', comment.parentId);
                    } else {
                        var replies = parent.replies;

                        replies.$all.push(comment);
                    }
                }
            });

            return roots;
        }
    };

    return {
        fetchPage: function (threadId, pageId, tree, onSuccess) {

            var start = new Date().getTime();

            Thread.discussion({id: threadId, page: pageId}, function (pageData) {

                var end = new Date().getTime();
                console.log('Fetch time: ' + (end - start));

                start = new Date().getTime();

                var comments = internal.postFetch(pageData.content, pageId);

                var roots = internal.sort(internal.merge(tree, comments));

                internal.shape(roots, {totalElementCount: pageData.totalElements});

                var page = {
                    id: pageId,
                    comments: roots
                };
                end = new Date().getTime();
                console.log('Execution time: ' + (end - start));

                onSuccess({page: page, isLastPage: pageData.lastPage});

            });
        }
    };
});
