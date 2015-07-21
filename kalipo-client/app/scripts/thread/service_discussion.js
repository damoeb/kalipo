'use strict';

kalipoApp.factory('Discussion', function ($http, Thread, $q, DISCUSSION_SHAPE_RULES) {

    var data = {
        threadId: null,
        roots: [],
        map: {},
        totalElementCount: null
    };

    var internal = {

        sort: function (comments) {
            return _.sortBy(comments, function (comment) {
                return -1 * comment.$score
            })
        },

        benchmark: function (comments) {

            return _.forEach(comments, function (comment, index) {
                comment.replies = [];
                comment.authors = [];
                var authorId = comment.displayName;

                if (comment.hidden) {
                    comment.text = 'Content hidden';
                    comment.dislikes = 0;
                    comment.likes = 0;
                    authorId = comment.id;
                }

                if (comment.status == 'DELETED') {
                    comment.displayName = 'Deleted';
                    comment.text = 'Content deleted';
                    comment.dislikes = 0;
                    comment.likes = 0;
                    authorId = comment.id;
                }

                if (_.isUndefined(comment.likes)) {
                    comment.likes = 0;
                }
                if (_.isUndefined(comment.dislikes)) {
                    comment.dislikes = 0;
                }

                comment.$pending = comment.status == 'PENDING';

                // todo minimize negative-only comments, hell-banned subthreads

                //comment.$hiddenreplies = comment.dislikes > 3 && comment.dislikes > comment.likes;
//                comment.$score = comment.influence / comment.createdDate;
//
                // author chose to hide his name
                if (_.isEmpty(comment.displayName) || _.isUndefined(comment.displayName)) {
                    comment.displayName = 'Anonymous';
//                    authorId = comment.id;
                }
//
//                comment.authors.push(authorId);

                var total = comment.likes + comment.dislikes;
                comment.$likes = comment.likes / total * 100;
                comment.$dislikes = comment.dislikes / total * 100;

            });
        },

        shape: function (comments, rules) {
            console.log('shape');
            internal.shapeRc(comments, 1, rules);
        },

        shapeRc: function (comments, level, rules) {

            _.forEach(comments, function (comment, index) {

                var $authors = [];
                $authors.push(comment.displayName);

                comment.$hasObligatoryReplies = false;

                comment.$repliesCount = 0;
                comment.$concealedRepliesCount = 0;

                rules.apply(comment, level, index, data);

                internal.shapeRc(comment.replies, level + 1, rules);

                comment.replies = internal.sort(comment.replies);

                _.forEach(comment.replies, function (reply, index) {

                    // get reply count
                    comment.$repliesCount += 1; // reply itself
                    comment.$repliesCount += reply.$repliesCount; // its replies

                    if(!reply.$obligatory) {
                        //console.log('optional', comment.id);
                        comment.$concealedRepliesCount ++;
                    } else {
                        comment.$hasObligatoryReplies = true;
                    }
                });


                // todo calc author diversity
                // todo authors not needed: if child is obligatory, self is optional -> self is obligatory+oneline
                //comment.$authors = _.uniq($authors);
                //
                //if (_.indexOf(comment.$authors, 'Modjo')) {
                //    console.log('authors comment', comment.id);
                //    comment.$obligatory = true;
                //}
                //
                //if(comment.$hasObligatoryReplies && !comment.$obligatory) {
                //    comment.$obligatory = true;
                //    comment.$oneline = true;
                //}

                comment.replies = internal.sort(comment.replies);

            });
        },

        merge: function (comments) {

            var roots = [];

            // create nested comments
            _.forEach(comments, function (comment) {

                data.map[comment.id] = comment;

                if (comment.level == 0 || _.isUndefined(comment.parentId)) {
                    roots.push(comment);

                } else {

                    var parent = data.map[comment.parentId];
                    if (_.isUndefined(parent)) {
                        console.log('cannot find parent of', comment.parentId);
                    } else {
                        var replies = parent.replies;
                        replies.push(comment);
                    }
                }
            });

            // todo sort should be applied to entire tree not fragment only
            internal.sort(roots);

            // merge with tree
            _.each(roots, function(root) {
                data.roots.push(root);
            })

            return roots;
        },

        templates: {
            'toggle_concealed': _.template('<div class="toggle-optionals" style="margin-left: <%- comment.level * 15 %>px; <% if(comment.level>1) { %>border-left: 1px dashed #ececec;<% } %>"><a href="javascript:void(0)" ng-click="toggleOptionals(\'<%- comment.id %>\')"><strong><% if(comment.$hasObligatoryReplies) {%> <%- comment.$concealedRepliesCount %><% } else { %><%- comment.$repliesCount %><% } %></strong> <% if(comment.$hasObligatoryReplies && comment.$concealedRepliesCount==1 || comment.$repliesCount==1) { %>reply<% } else { %>replies<% } %></a> <span class="glyphicon glyphicon-chevron-down"></span></div>')
        },

        findPageWithComment: function (commentId, response) {
            var deferred = $q.defer();

            if(!_.isUndefined(commentId) && _.isUndefined(data.map[commentId])) {
                console.log('fetch loop for', commentId)
                deferred.resolve([response]);

                // todo load all pages until found
//                Thread.discussion({id: data.threadId, from: commentId}, function (response) {
//                    // todo merge into data
//                    deferred.resolve([response]);
//                });

            } else {
                console.log('no comment requested');
                deferred.resolve([response]);
            }

            return deferred.promise;
        },
    };

    var deferInit;

    return {

        init: function (threadId) {

            if (_.isUndefined(deferInit)) {
                deferInit = $q.defer();

                var promiseComment = $http.get('views/template_comment.html', {cache: true});
                var promiseMenu = $http.get('views/template_menu.html', {cache: true});

                $q.all([promiseComment, promiseMenu]).then(function (response) {
                    internal.templates['comment'] = _.template(response[0].data);
                    internal.templates['menu'] = _.template(response[1].data);
                    deferInit.resolve();
                });

                data.threadId = threadId;
                data.roots = [];
                data.map = {};
            }
            return deferInit.promise;
        },

        renderComment: function (comment, $sink, concealed) {

            var comp_comment = internal.templates['comment'];
            var comp_menu = internal.templates['menu'];
            var comp_toggle_concealed = internal.templates['toggle_concealed'];

            var __render = function (comment, $sink, concealed) {

                var $comment = $(comp_comment({
                    comment: comment,
                    fnRenderMenu: comp_menu
                })).appendTo($sink);

                var $replies = $('<div></div>', {class: 'replies'}).appendTo($comment);

                // obligatory replies
                _.forEach(comment.replies, function (reply) {
                    if (reply.$obligatory || concealed) {
                        __render(reply, $replies, concealed);
                    }
                });

                if (comment.$concealedRepliesCount > 0 && !concealed) {

                    $comment.append(comp_toggle_concealed({
                        comment: comment
                    }));

                    var $hidden_replies = $('<div></div>', {class: 'replies optionals hidden'}).appendTo($comment);

                    // obligatory replies
                    _.forEach(comment.replies, function (reply) {
                        if (!reply.$obligatory) {
                            __render(reply, $hidden_replies, true);
                        }
                    });
                }
            };

            __render(comment, $sink, concealed);
        },

        firstFetch: function (commentId) {

            var defer = $q.defer();

            var fetchHead = Thread.discussion({id: data.threadId});

            // todo shit
            $q.when(fetchHead)
//            .then(function(response) {
//                return internal.findPageWithComment(commentId, response)
//            })
            .then(function (responses) {

                console.log('responses', responses);

                _.each(responses, function(response) {

                    var comments = response.content;
                    internal.benchmark(comments);
                    data.totalElementCount = response.totalElements;

                    console.log('response', response);
                    console.log('comments', comments);

                    var roots = internal.merge(comments);

                    internal.shape(roots, DISCUSSION_SHAPE_RULES);

                    var fragment = {
                        comments: roots,
                        from: null,
                        to: null,
                        meta: {
                            isLastPage: response.lastPage,
                            isFirstPage: response.firstPage,
                            numberOfElements: response.numberOfElements,
                            totalElements: response.totalElements
                        }
                    };

                    console.log('fragment', fragment);

                    defer.resolve({fragment:fragment, roots:data.roots});
                })
            })

            return defer.promise;
        }
    };
});
