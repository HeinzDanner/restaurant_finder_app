package de.hepaza.demo.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AppUserRepository appUserRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Userdaten von GitHub holen
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. User in DB? getName() holt die ID des eingeloggten GitHub Users
        AppUser appUser = appUserRepository.findById(oAuth2User.getName())
                .orElseGet(() -> createAndSaveUser(oAuth2User));

        // 3. NameAttributeKey wird aus Client-Registration gelesen (bei GitHub ist das die "id")
        String nameAttributeKey = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        // 4. Oauth2user mit Rolle aus DB zurückgeben
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(appUser.role())),
                oAuth2User.getAttributes(),
                nameAttributeKey
        );
    }

    private AppUser createAndSaveUser(OAuth2User oAuth2User) {
        AppUser appUser = AppUser.builder()
                .id(oAuth2User.getName())
                .username(oAuth2User.getAttribute("login"))
                .avatarUrl(oAuth2User.getAttribute("avatar_url"))
                .role("USER")
                .build();
        return appUserRepository.save(appUser);
    }
}