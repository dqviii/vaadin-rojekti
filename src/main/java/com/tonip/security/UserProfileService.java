package com.tonip.security;

import com.tonip.security.domain.User;
import com.tonip.security.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserProfileService {

    public static final long MAX_PROFILE_PICTURE_BYTES = 5L * 1024 * 1024;

    private final UserRepository userRepository;

    UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void updateProfilePicture(String username, byte[] bytes, String mimeType) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
        user.setProfilePicture(bytes);
        user.setProfilePictureMimeType(mimeType);
        userRepository.save(user);
    }

    @Transactional
    public void removeProfilePicture(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
        user.setProfilePicture(null);
        user.setProfilePictureMimeType(null);
        userRepository.save(user);
    }
}
