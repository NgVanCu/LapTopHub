package com.laptophub.auth.service.impl;

import com.laptophub.auth.service.EmailVerificationService;
import com.laptophub.shared.exception.AppException;
import com.laptophub.shared.exception.ErrorCode;
import com.laptophub.shared.properties.EmailVerificationProperties;
import com.laptophub.shared.service.EmailService;
import com.laptophub.shared.util.EmailNormalizer;
import com.laptophub.shared.util.EmailVerificationTokenGenerator;
import com.laptophub.shared.util.HashToken;
import com.laptophub.user.entity.User;
import com.laptophub.user.enums.UserStatus;
import com.laptophub.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final UserService userService;
    private final EmailVerificationProperties emailVerificationProperties;
    private final EmailService emailService;

    public EmailVerificationServiceImpl(UserService userService, EmailVerificationProperties emailVerificationProperties,
                                        EmailService emailService) {
        this.userService = userService;
        this.emailVerificationProperties = emailVerificationProperties;
        this.emailService = emailService;
    }

    @Transactional
    public void verify(String rawToken) {
        String tokenHash = HashToken.hash(rawToken);
        User user = userService.findByEmailVerificationTokenHash(tokenHash)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_VERIFICATION_TOKEN_INVALID));

        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            // Phòng vệ: BLOCKED sau khi có token nhưng trước khi kịp verify.
            // Không dùng verify-email để lộ trạng thái tài khoản.
            throw new AppException(ErrorCode.EMAIL_VERIFICATION_TOKEN_INVALID);
        }
        if (user.getEmailVerificationExpiresAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.EMAIL_VERIFICATION_TOKEN_EXPIRED);
        }

        int rows = userService.verifyEmailIfPending(user.getId());
        if (rows == 0) {
            // Thua cuộc đua hiếm gặp (2 request verify đồng thời cùng
            // token) — không bịa thêm mã lỗi riêng cho nhánh gần như không
            // thể xảy ra, dùng chung fallback INVALID.
            throw new AppException(ErrorCode.EMAIL_VERIFICATION_TOKEN_INVALID);
        }
    }

    @Transactional
    public void resend(String rawEmail) {
        String normalizedEmail = EmailNormalizer.normalize(rawEmail);
        User user = userService.findByNormalizedEmail(normalizedEmail).orElse(null);
        if (user == null || user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            return;
        }

        String rawToken = EmailVerificationTokenGenerator.generate();
        String tokenHash = HashToken.hash(rawToken);
        Instant expiresAt = Instant.now().plus(emailVerificationProperties.ttl());

        // Ghi đè plain (không gate) — không phải chuyển trạng thái, chỉ làm
        // mới token. 2 lần resend gần nhau có thể khiến token của email gửi
        // trước không còn khớp DB nữa dù đã gửi thành công — chấp nhận là
        // technical debt MVP, không xử lý đợt này (xem plan).
        userService.reissueEmailVerificationToken(user.getId(), tokenHash, expiresAt);

        String link = emailVerificationProperties.verifyUrl() + "?token=" + rawToken;
        String subject = "Gửi lại email xác thực tài khoản LaptopHub";
        String body = "Chào " + user.getFullName() + ",<br/><br/>"
                + "Bạn vừa yêu cầu gửi lại email xác thực tài khoản LaptopHub. Vui lòng nhấn vào liên kết bên dưới "
                + "để xác thực địa chỉ email và kích hoạt tài khoản của bạn:<br/><br/>"
                + "<a href=\"" + link + "\">" + link + "</a><br/><br/>"
                + "Liên kết có hiệu lực trong " + emailVerificationProperties.ttl().toHours() + " giờ. Nếu bạn "
                + "không thực hiện yêu cầu này, vui lòng bỏ qua email.<br/><br/>"
                + "Trân trọng,<br/>Đội ngũ LaptopHub";
        emailService.send(user.getEmail(), subject, body);
    }
}
