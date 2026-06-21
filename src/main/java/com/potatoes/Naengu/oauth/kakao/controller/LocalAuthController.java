package com.potatoes.Naengu.oauth.kakao.controller;

import com.potatoes.Naengu.oauth.kakao.domain.model.UserEntity;
import com.potatoes.Naengu.oauth.kakao.dto.AuthTokens;
import com.potatoes.Naengu.oauth.kakao.dto.LoginSuccessResponse;
import com.potatoes.Naengu.oauth.kakao.repository.UserRepository;
import com.potatoes.Naengu.oauth.kakao.service.AuthTokensGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Profile("local")
@RestController
@RequiredArgsConstructor
@RequestMapping("/local/auth")
public class LocalAuthController {

    private final UserRepository userRepository;
    private final AuthTokensGenerator authTokensGenerator;

    @PostMapping("/login")
    public ResponseEntity<LoginSuccessResponse> login(
            @RequestParam(defaultValue = "1") Long providerId,
            @RequestParam(defaultValue = "local-user") String nickname
    ) {
        boolean isNewMember = false;

        UserEntity user = userRepository.findByProviderId(providerId)
                .orElse(null);

        if (user == null) {
            user = new UserEntity();
            user.setProviderId(providerId);
            user.setNickName(nickname);
            isNewMember = true;
        } else {
            if (!user.getNickName().equals(nickname)) {
                user.setNickName(nickname);
            }

            if (user.isDeleted()) {
                user.setDeleted(false);
                user.setNickName(nickname);
                isNewMember = true;
            }
        }

        userRepository.save(user);

        AuthTokens tokens = authTokensGenerator.generate(providerId.toString());

        LoginSuccessResponse response = new LoginSuccessResponse(
                user.getProviderId(),
                user.getNickName(),
                tokens.getGrantType(),
                tokens.getAccessToken(),
                tokens.getExpiresIn(),
                isNewMember
        );

        return ResponseEntity.ok(response);
    }
}
