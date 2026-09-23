package com.daka.server.service;

import com.daka.server.entity.User;
import com.daka.server.entity.UserProgress;
import com.daka.server.repository.UserProgressRepository;
import com.daka.server.repository.UserRepository;
import com.daka.server.util.JwtUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserProgressRepository progressRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, UserProgressRepository progressRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.progressRepository = progressRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public String login(String username, String password) {
        if (username == null || username.isBlank() || username.length() > 50) {
            throw new IllegalArgumentException("用户名不合法");
        }
        if (password == null || password.isBlank() || password.length() > 100) {
            throw new IllegalArgumentException("密码不合法");
        }
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            user = registerUser(username, password);
        } else if (!matchesAndUpgrade(user, password)) {
            throw new IllegalArgumentException("密码错误");
        }
        return jwtUtil.generateToken(user.getUserId(), user.getUsername());
    }

    /**
     * 校验密码。历史数据存的是明文,明文比对通过后升级为 BCrypt 哈希。
     */
    private boolean matchesAndUpgrade(User user, String rawPassword) {
        String stored = user.getPassword();
        if (stored != null && stored.startsWith("$2")) {
            return passwordEncoder.matches(rawPassword, stored);
        }
        if (stored.equals(rawPassword)) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    private User registerUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setUserId(UUID.randomUUID().toString());
        user.setNickname(username);
        try {
            user = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            // 并发首登同一用户名触发唯一约束冲突,按已存在账号重新校验密码
            user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("注册失败"));
            if (!matchesAndUpgrade(user, password)) {
                throw new IllegalArgumentException("密码错误");
            }
            return user;
        }

        UserProgress progress = new UserProgress();
        progress.setUserId(user.getUserId());
        progressRepository.save(progress);
        return user;
    }

    public User getUserByToken(String token) {
        String userId = jwtUtil.getUserIdFromToken(token);
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }
}
