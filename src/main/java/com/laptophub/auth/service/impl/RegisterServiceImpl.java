package com.laptophub.auth.service.impl;

import com.laptophub.auth.dto.request.RegisterRequest;
import com.laptophub.auth.service.RegisterService;
import com.laptophub.shared.properties.EmailVerificationProperties;
import com.laptophub.shared.service.EmailService;
import com.laptophub.shared.util.EmailNormalizer;
import com.laptophub.shared.util.EmailVerificationTokenGenerator;
import com.laptophub.shared.util.HashToken;
import com.laptophub.user.entity.User;
import com.laptophub.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RegisterServiceImpl implements RegisterService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationProperties emailVerificationProperties;
    private final EmailService emailService;

    public RegisterServiceImpl(UserService userService, PasswordEncoder passwordEncoder,
                               EmailVerificationProperties emailVerificationProperties,
                               EmailService emailService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationProperties = emailVerificationProperties;
        this.emailService = emailService;
    }

    @Override
    public User register(RegisterRequest request){
        
        String normalizedEmail = EmailNormalizer.normalize(request.email());
        String passwordHash = passwordEncoder.encode(request.password());

        String rawToken = EmailVerificationTokenGenerator.generate();
        String tokenHash = HashToken.hash(rawToken);
        Instant expiresAt = Instant.now()
                .plus(emailVerificationProperties.ttl());
        User user = userService.createCustomer(normalizedEmail, passwordHash, request.fullName(), request.phone(),
                tokenHash, expiresAt);

        sendVerificationEmail(user, rawToken);

        return user;
    }

    private void sendVerificationEmail(User user, String rawToken) {
        String link = emailVerificationProperties.verifyUrl() + "?token=" + rawToken;
        String subject = "Xác thực tài khoản LaptopHub";
        String body = "Chào " + user.getFullName() + ",<br/><br/>"
                + "Cảm ơn bạn đã đăng ký tài khoản tại LaptopHub. Vui lòng nhấn vào liên kết bên dưới để xác thực "
                + "địa chỉ email và kích hoạt tài khoản của bạn:<br/><br/>"
                + "<a href=\"" + link + "\">" + link + "</a><br/><br/>"
                + "Liên kết có hiệu lực trong " + emailVerificationProperties.ttl().toHours() + " giờ. Nếu bạn "
                + "không thực hiện yêu cầu đăng ký này, vui lòng bỏ qua email.<br/><br/>"
                + "Trân trọng,<br/>Đội ngũ LaptopHub";
        emailService.send(user.getEmail(), subject, body);
    }
}
