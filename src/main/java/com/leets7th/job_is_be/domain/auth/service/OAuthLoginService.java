package com.leets7th.job_is_be.domain.auth.service;

import com.leets7th.job_is_be.domain.auth.oauth.OAuthClientRegistry;
import com.leets7th.job_is_be.domain.auth.oauth.OAuthStateStore;
import com.leets7th.job_is_be.domain.auth.oauth.OAuthUserInfo;
import com.leets7th.job_is_be.domain.auth.oauth.SocialOAuthClient;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.entity.UserWithdrawal;
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.domain.user.enums.UserStatus;
import com.leets7th.job_is_be.domain.user.enums.WithdrawalStatus;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.domain.user.repository.UserWithdrawalRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.jwt.JwtTokenProvider;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class OAuthLoginService {

    private final OAuthClientRegistry clientRegistry;
    private final OAuthStateStore stateStore;
    private final UserRepository userRepository;
    private final UserWithdrawalRepository userWithdrawalRepository;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenSessionStore refreshTokenSessionStore;

    public OAuthLoginService(
            OAuthClientRegistry clientRegistry,
            OAuthStateStore stateStore,
            UserRepository userRepository,
            UserWithdrawalRepository userWithdrawalRepository,
            JwtTokenProvider tokenProvider,
            RefreshTokenSessionStore refreshTokenSessionStore
    ) {
        this.clientRegistry = clientRegistry;
        this.stateStore = stateStore;
        this.userRepository = userRepository;
        this.userWithdrawalRepository = userWithdrawalRepository;
        this.tokenProvider = tokenProvider;
        this.refreshTokenSessionStore = refreshTokenSessionStore;
    }

    public AuthorizationRequest createAuthorizationRequest(String provider) {
        SocialType socialType = clientRegistry.parseProvider(provider);
        SocialOAuthClient client = clientRegistry.get(socialType);
        String state = stateStore.create(socialType);
        return new AuthorizationRequest(client.buildAuthorizationUri(state), state);
    }

    @Transactional
    public OAuthLoginResult login(String provider, String code, String state, String cookieState) {
        SocialType socialType = clientRegistry.parseProvider(provider);
        if (code == null || code.isBlank()) {
            throw new GeneralException(ErrorStatus.OAUTH_CODE_MISSING);
        }
        if (!stateStore.consume(state, cookieState, socialType)) {
            throw new GeneralException(ErrorStatus.OAUTH_STATE_INVALID);
        }

        OAuthUserInfo userInfo = clientRegistry.get(socialType).getUserInfo(code);
        LoginUser loginUser = findOrCreateUser(userInfo);
        JwtTokenProvider.TokenPair tokenPair = tokenProvider.issueTokenPair(loginUser.user().getId());
        refreshTokenSessionStore.save(
                tokenPair.refreshSessionId(),
                loginUser.user().getId(),
                tokenPair.refreshToken(),
                tokenPair.refreshTokenTtl()
        );

        return new OAuthLoginResult(tokenPair.refreshToken(), loginUser.newUser());
    }

    private LoginUser findOrCreateUser(OAuthUserInfo userInfo) {
        return userRepository
                .findBySocialIdAndSocialType(userInfo.socialId(), userInfo.socialType())
                .map(user -> new LoginUser(restoreIfWithdrawn(user), false))
                .orElseGet(() -> createUser(userInfo));
    }

    private LoginUser createUser(OAuthUserInfo userInfo) {
        String normalizedEmail = userInfo.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new GeneralException(ErrorStatus.SOCIAL_ACCOUNT_CONFLICT);
        }

        User user = User.builder()
                .socialId(userInfo.socialId())
                .socialType(userInfo.socialType())
                .email(normalizedEmail)
                .build();
        return new LoginUser(userRepository.save(user), true);
    }

    private User restoreIfWithdrawn(User user) {
        if (user.getStatus() != UserStatus.WITHDRAWN) {
            return user;
        }

        UserWithdrawal withdrawal = userWithdrawalRepository
                .findFirstByUserIdAndStatusOrderByRequestedAtDesc(user.getId(), WithdrawalStatus.PENDING)
                .orElseThrow(() -> new GeneralException(ErrorStatus.WITHDRAWAL_RESTORE_EXPIRED));
        LocalDateTime now = LocalDateTime.now();
        if (withdrawal.getScheduledDeletionAt() == null
                || !now.isBefore(withdrawal.getScheduledDeletionAt())) {
            throw new GeneralException(ErrorStatus.WITHDRAWAL_RESTORE_EXPIRED);
        }

        user.restore();
        withdrawal.restore(now);
        return user;
    }

    private record LoginUser(User user, boolean newUser) {
    }

    public record OAuthLoginResult(String refreshToken, boolean newUser) {
    }

    public record AuthorizationRequest(URI uri, String state) {
    }
}
