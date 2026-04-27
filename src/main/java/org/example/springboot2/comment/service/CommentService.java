package org.example.springboot2.comment.service;

import org.example.springboot2.comment.entity.Comment;
import org.example.springboot2.comment.repository.CommentRepository;
import org.example.springboot2.interaction.like.service.LikeService;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    public Map<String, Object> getCommentTree(int pageNo, int pageSize, String token,
                                              String targetType, Long targetId) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Comment> page;

        if (targetType != null && targetId != null) {
            page = commentRepository.findByTargetTypeAndTargetIdAndParentIdIsNullOrderByCreateTimeDesc(
                    targetType, targetId, pageable);
        } else {
            page = commentRepository.findByTargetTypeIsNullAndParentIdIsNullOrderByCreateTimeDesc(pageable);
        }

        List<Comment> topComments = page.getContent();

        User currentUser = null;
        if (token != null && !token.isEmpty()) {
            currentUser = userService.getUserByToken(token);
        }
        final Long currentUserId = currentUser != null ? currentUser.getId() : null;

        List<Map<String, Object>> treeList = topComments.stream()
                .map(comment -> buildCommentNode(comment, currentUserId))
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("items", treeList);
        result.put("total", page.getTotalElements());
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        return result;
    }

    private Map<String, Object> buildCommentNode(Comment comment, Long currentUserId) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", comment.getId());
        node.put("parentId", comment.getParentId());
        node.put("content", comment.getContent());
        node.put("userId", comment.getUserId());
        node.put("username", comment.getUsername());
        node.put("avatar", comment.getAvatar());
        node.put("likeCount", comment.getLikeCount());
        node.put("createTime", comment.getCreateTime());

        boolean liked = currentUserId != null && likeService.isLiked(currentUserId, "comment", comment.getId());
        node.put("liked", liked);

        List<Comment> children = commentRepository.findByParentIdOrderByCreateTimeAsc(comment.getId());
        List<Map<String, Object>> childNodes = children.stream()
                .map(child -> buildCommentNode(child, currentUserId))
                .collect(Collectors.toList());
        node.put("children", childNodes);
        node.put("replyCount", children.size());
        return node;
    }

    @Transactional
    public Comment addComment(String token, Long parentId, String content,
                              String targetType, Long targetId) {
        User user = userService.getUserByToken(token);
        if (user == null) throw new RuntimeException("用户未登录或Token无效");
        Comment comment = new Comment();
        comment.setParentId(parentId);
        comment.setContent(content);
        comment.setUserId(user.getId());
        comment.setUsername(user.getUsername());
        comment.setAvatar(user.getAvatar());
        comment.setTargetType(targetType);
        comment.setTargetId(targetId);
        return commentRepository.save(comment);
    }

    @Transactional
    public Comment updateComment(String token, Long commentId, String newContent) {
        User currentUser = userService.getUserByToken(token);
        if (currentUser == null) throw new RuntimeException("用户未登录或Token无效");

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        boolean isAuthor = comment.getUserId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoles() != null && currentUser.getRoles().contains("admin");

        if (!isAuthor && !isAdmin) {
            throw new RuntimeException("无权修改他人评论");
        }

        comment.setContent(newContent);
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(String token, Long commentId) {
        User currentUser = userService.getUserByToken(token);
        if (currentUser == null) throw new RuntimeException("用户未登录或Token无效");

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        boolean isAuthor = comment.getUserId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoles() != null && currentUser.getRoles().contains("admin");

        if (!isAuthor && !isAdmin) {
            throw new RuntimeException("无权删除他人评论");
        }

        commentRepository.delete(comment);
    }
}