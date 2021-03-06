package org.kalipo.repository;

import org.joda.time.DateTime;
import org.kalipo.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Set;

/**
 * Spring Data MongoDB repository for the Comment entity.
 */
public interface CommentRepository extends MongoRepository<Comment, String> {

    // todo remove threadId, body from results
    @Query(value = "{'threadId': ?0, 'status': { $in: ?1} }", fields = "{ 'id':1, 'parentId':1, 'threadId':1, 'createdDate':1, 'bodyHtml':1, 'displayName':1, 'level':1, 'influence':1, 'likes':1, 'dislikes':1, 'status':1, 'lastModifiedDate':1, 'createdByMod':1 }")
    Page<Comment> findByThreadIdAndStatusIn(String id, List<Comment.Status> status, Pageable pageable);

    Page<Comment> findByAuthorId(String userId, Pageable pageable);

    @Query(value = "{'authorId': ?0, 'status': 'APPROVED'}", count = true)
    Long getApprovedCommentCountOfUser(String userId);

    @Query(value = "{'authorId': ?0, 'status': 'REJECTED'}", count = true)
    Long getRejectedCommentCountOfUser(String login);

    @Query(value = "{'authorId': ?0, 'status': 'DELETED'}", count = true)
    Long getDeletedCommentCountOfUser(String login);

    @Query(value = "{'parentId': ?0}", count = true)
    Long countReplies(String commentId);

    @Query(value = "{'authorId': ?0, 'createdDate': {$gte: ?1, $lt: ?2}}", count = true)
    int countWithinDateRange(String currentLogin, DateTime from, DateTime to);

    @Query(value = "{'threadId': ?0, 'status' : 'APPROVED'}", count = true)
    int countApprovedInThread(String threadId);

    @Query(value = "{'threadId': ?0, 'status' : 'PENDING'}", count = true)
    int countPendingInThread(String threadId);

    @Query(value = "{'threadId': ?0, 'reportedCount' : {$gt: 0}}", count = true)
    int countReportedInThread(String threadId);

    Page<Comment> findByStatus(Comment.Status status, Pageable pageable);

    List<Comment> findByThreadId(String threadId, Sort sort);

    Set<Comment> findByParentId(String id);
}
