package be.kuleuven.distributedsystems.cloud.auth;

import be.kuleuven.distributedsystems.cloud.FirestoreService;
import be.kuleuven.distributedsystems.cloud.entities.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(FirestoreService.class);

    private static RSAPublicKey fetchPublicKey(String kid) {
        try {
            String JWKS_URL = "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com";

            HttpURLConnection connection = (HttpURLConnection) new URL(JWKS_URL).openConnection();
            try (InputStream is = connection.getInputStream()) {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> jwkMap = objectMapper.readValue(is, new TypeReference<>() {});
                String x509Certificate = jwkMap.get(kid);
                
                return convertX509ToRSAPublicKey(x509Certificate.replaceAll("-----BEGIN CERTIFICATE-----|-----END CERTIFICATE-----|\\n", ""));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error fetching and parsing public keys", e);
        }
    }

    private static RSAPublicKey convertX509ToRSAPublicKey(String x509Certificate) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(x509Certificate);
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(decodedBytes));

            // Get the public key from the certificate
            PublicKey publicKey = certificate.getPublicKey();

            // Ensure the public key is an RSAPublicKey
            if (publicKey instanceof RSAPublicKey) {
                return (RSAPublicKey) publicKey;
            } else {
                throw new RuntimeException("The public key is not an RSAPublicKey");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error converting X.509 certificate to RSAPublicKey", e);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // TODO: (level 1) decode Identity Token and assign correct email and role

        try {
            String identityToken = request.getHeader("Authorization").split(" ")[1];
            String kid = JWT.decode(identityToken).getKeyId();
            RSAPublicKey pubKey = fetchPublicKey(kid);

            if (pubKey != null) {
                Algorithm algorithm = Algorithm.RSA256(pubKey, null);
                String projectId = "ds-part-2";
                DecodedJWT jwt = JWT.require(algorithm)
                                    .withIssuer("https://securetoken.google.com/" + projectId)
                                    .build()
                                    .verify(identityToken);

                String email = jwt.getClaim("email").asString();
                String[] roles = jwt.getClaim("roles").asArray(String.class);

                var user = new User(email, roles);

                SecurityContext context = SecurityContextHolder.getContext();
                context.setAuthentication(new FirebaseAuthentication(user));

            } else {
                logger.info("Security Filter: Trying Localhost");
                DecodedJWT jwt = JWT.decode(identityToken);

                String email = jwt.getClaim("email").asString();
                String[] roles = jwt.getClaim("roles").asArray(String.class);

                var user = new User(email, roles);

                SecurityContext context = SecurityContextHolder.getContext();
                context.setAuthentication(new FirebaseAuthentication(user));
            }

        } catch (Exception e){
            logger.error(e.getMessage());
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        filterChain.doFilter(request, response);

    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !(path.startsWith("/api"));
    }

    public static User getUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private static class FirebaseAuthentication implements Authentication {
        private final User user;

        FirebaseAuthentication(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            if (user.isManager()) {
                return List.of(new SimpleGrantedAuthority("manager"));
            } else {
                return new ArrayList<>();
            }
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getDetails() {
            return null;
        }

        @Override
        public User getPrincipal() {
            return this.user;
        }

        @Override
        public boolean isAuthenticated() {
            return true;
        }

        @Override
        public void setAuthenticated(boolean b) throws IllegalArgumentException {

        }

        @Override
        public String getName() {
            return null;
        }
    }
}

