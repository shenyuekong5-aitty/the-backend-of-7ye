package org.example.springboot2.comment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.springboot2.comment.entity.Comment;
import org.example.springboot2.comment.repository.CommentRepository;
import org.example.springboot2.interaction.like.service.LikeService;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.example.springboot2.websocket.WebSocketServer;
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

    @Autowired
    private ObjectMapper objectMapper; // 用于序列化 WebSocket 消息

    public Map<String, Object> getCommentTree(int pageNo, int pageSize, String token,
                                              String targetType, Long targetId,boolean allComments) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Comment> page;

        if (targetType != null && targetId != null) {
            // 特定目标评论
            page = commentRepository.findByTargetTypeAndTargetIdAndParentIdIsNullOrderByCreateTimeDesc(targetType, targetId, pageable);
        } else if (targetType != null) {
            // 特定类型的所有评论
            page = commentRepository.findByTargetTypeAndParentIdIsNullOrderByCreateTimeDesc(targetType, pageable);
        } else if (allComments) {
            // 所有顶级评论（不分目标）
            page = commentRepository.findByParentIdIsNullOrderByCreateTimeDesc(pageable);
        } else {
            // 默认：只加载留言板评论（target_type IS NULL）
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
        node.put("nickname", comment.getNickname());
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
        comment.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
        comment.setAvatar(user.getAvatar());
        comment.setTargetType(targetType);
        comment.setTargetId(targetId);

        Comment saved = commentRepository.save(comment);

        // WebSocket 实时推送新评论
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("type", "new_comment");
            msg.put("targetType", targetType);
            msg.put("targetId", targetId);
            msg.put("data", saved);
            String jsonMsg = objectMapper.writeValueAsString(msg);
            WebSocketServer.broadcast(jsonMsg);
        } catch (Exception e) {
            // 推送失败不应影响主流程，仅记录日志
            e.printStackTrace();
        }

        return saved;
    }

    @Transactional
    public Comment updateComment(String token, Long commentId, String newContent) {
        User currentUser = userService.getUserByToken(token);
        if (currentUser == null) throw new RuntimeException("用户未登录或Token无效");

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        boolean isAuthor = comment.getUserId().equals(currentUser.getId());
        boolean isAdmin = "admin".equals(currentUser.getRole());

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
        boolean isAdmin = "admin".equals(currentUser.getRole());

        if (!isAuthor && !isAdmin) {
            throw new RuntimeException("无权删除他人评论");
        }

        commentRepository.delete(comment);
    }
}