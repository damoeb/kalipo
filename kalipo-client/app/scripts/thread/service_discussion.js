'use strict';

kalipoApp.factory('Discussion', function ($http, Thread, $q, DISCUSSION_SHAPE_RULES) {

    var internal = {

        sort: function (comments) {
            return _.sortBy(comments, function (comment) {
                return -1 * comment.$score
            })
        },

        postFetch: function (comments, pageId) {
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
                comment.$score = comment.influence / comment.createdDate;

                // author chose to hide his name
                if (_.isEmpty(comment.displayName) || _.isUndefined(comment.displayName)) {
                    comment.displayName = 'Anonymous';
                    authorId = comment.id;
                }

                comment.authors.push(authorId);

                var total = comment.likes + comment.dislikes;
                comment.$likes = comment.likes / total * 100;
                comment.$dislikes = comment.dislikes / total * 100;

            });
        },

        shape: function (comments, rules, properties) {
            internal.shapeRc(comments, 1, rules, properties);
        },

        shapeRc: function (comments, level, rules, properties) {

            _.forEach(comments, function (comment, index) {

                var $authors = [];
                $authors.push(comment.displayName);

                comment.$hasObligatoryReplies = false;

                comment.$repliesCount = 0;
                comment.$concealedRepliesCount = 0;

                rules.apply(comment, level, index, properties);

                internal.shapeRc(comment.replies, level + 1, rules, properties);

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
                        replies.push(comment);
                    }
                }
            });

            return roots;
        },
        templates: {
            'toggle_concealed': _.template('<div class="toggle-optionals" style="margin-left: <%- comment.level * 15 %>px; <% if(comment.level>1) { %>border-left: 1px dashed #ececec;<% } %>"><a href="javascript:void(0)" ng-click="toggleOptionals(\'<%- comment.id %>\')"><strong><% if(comment.$hasObligatoryReplies) {%> <%- comment.$concealedRepliesCount %><% } else { %><%- comment.$repliesCount %><% } %></strong> <% if(comment.$hasObligatoryReplies && comment.$concealedRepliesCount==1 || comment.$repliesCount==1) { %>reply<% } else { %>replies<% } %></a> <span class="glyphicon glyphicon-chevron-down"></span></div>')
        }
    };

    var deferInit;

    return {

        init: function () {

            if (_.isUndefined(deferInit)) {
                deferInit = $q.defer();

                var promiseComment = $http.get('views/template_comment.html', {cache: true});
                var promiseMenu = $http.get('views/template_menu.html', {cache: true});

                $q.all([promiseComment, promiseMenu]).then(function (response) {
                    internal.templates['comment'] = _.template(response[0].data);
                    internal.templates['menu'] = _.template(response[1].data);
                    deferInit.resolve();
                });
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

        scrollTo: function (commentId) {
            var $element = $('#' + commentId);
            if($element.length > 0) {
                // show element if in hidden branch
                if (!$element.is(':visible')) {
                    $element.parents('.replies.optionals').removeClass('hidden');
                }
                $('html, body').scrollTop($element.offset().top);
            }
        },

        fetch: function (threadId, pageId, tree) {

            var defer = $q.defer();

            Thread.discussion({id: threadId, page: pageId}, function (pageData) {

                var comments = internal.postFetch(pageData.content, pageId);

                var roots = internal.sort(internal.merge(tree, comments));

                var totalElementCount = pageData.totalElements;

                internal.shape(roots, DISCUSSION_SHAPE_RULES, {
                    totalElementCount: pageData.totalElements
                });

                var page = {
                    id: pageId,
                    comments: roots
                };

                defer.resolve({page: page, isLastPage: pageData.lastPage, isFirstPage: pageData.firstPage, totalElements: pageData.totalElements, numberOfElements: pageData.numberOfElements});

            });

            return defer.promise;
        }
    };
});
