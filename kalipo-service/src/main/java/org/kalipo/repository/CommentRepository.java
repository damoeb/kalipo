package org.kalipo.repository;

import org.joda.time.DateTime;
import org.kalipo.domain.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;

/**
 * Spring Data MongoDB repository for the Comment entity.
 */
public interface CommentRepository extends MongoRepository<Comment, String> {

    public final static String FIND_BY_THREAD_AND_STATUS_QUERY = "{ 'threadId': ?0, 'status': { $in: ?1 }}";

    @Query(FIND_BY_THREAD_AND_STATUS_QUERY)
    List<Comment> findByThreadIdAndStatus(String id, Collection<Comment.Status> status);

    List<Comment> findByStatus(Comment.Status status, Pageable pageable);

    @Query(value = "{'authorId': ?0, 'status': 'APPROVED'}", count = true)
    Long getApprovedCommentCountOfUser(String userId);

    @Query(value = "{'parentId': ?0}", count = true)
    Long countReplies(String commentId);

    @Query(value = "{'authorId': ?0, 'createdDate': {$gte: ?1, $lt: ?2}}", count = true)
    int countWithinDateRange(String currentLogin, DateTime from, DateTime to);
}
