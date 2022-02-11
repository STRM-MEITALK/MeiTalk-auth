package com.meitalk.auth.config.auth;

import com.meitalk.auth.mapper.UserMapper;
import com.meitalk.auth.model.ResponseWithData;
import com.meitalk.auth.model.channel.ReqChannel;
import com.meitalk.auth.model.user.CustomUserDetails;
import com.meitalk.auth.model.user.OAuthAttributes;
import com.meitalk.auth.model.user.UserJoinDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserMapper userMapper;
    @Value("${meitalk.api.server}")
    String meitalkApiServer;
    @Value("${meitalk.api.server.create-channel-url}")
    String createChannelUrl;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // google, facebook
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = null;
        if (registrationId.equalsIgnoreCase("google")) {
            attributes = attributes.ofG(registrationId, userNameAttributeName, oAuth2User.getAttributes());
            log.info("google login start : {}", attributes.toString());
        } else if (registrationId.equalsIgnoreCase("facebook")) {
            attributes = attributes.ofF(registrationId, userNameAttributeName, oAuth2User.getAttributes());
            log.info("facebook login start : {}", attributes.toString());
        }

        CustomUserDetails user = saveUser(attributes);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }

    private CustomUserDetails saveUser(OAuthAttributes attributes) {
        CustomUserDetails user = userMapper.selectUserByEmail(attributes.getEmail());
        if (user == null) {
            UserJoinDto dto = null;
            if (attributes.getRegistraionId().equalsIgnoreCase("google")) {
                dto = UserJoinDto.builder()
                        .mailId(attributes.getEmail())
                        .userName(attributes.getName())
                        .platform(attributes.getRegistraionId())
                        .privacyAgree("Y")
                        .userPicture(attributes.getPicture())
                        .oauth2Key(attributes.getAttributes().get("sub").toString())
                        .googleOauth2Key(attributes.getAttributes().get("sub").toString())
                        .facebookOauth2Key(null)
                        .appleOauth2Key(null)
                        .emailVerification("Y")
                        .build();
                log.info("first google login");
                log.info("sign up for google email : {}", dto);
                ReqChannel.CreateInternalChannel createInternalChannel = ReqChannel.CreateInternalChannel.builder()
                        .userNo(dto.getId())
                        .build();
                WebClient webClient = WebClient.builder()
                        .baseUrl(meitalkApiServer)
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .build();
                ResponseWithData responseWithData = webClient.post()
                        .uri(createChannelUrl)
                        .body(Mono.just(createInternalChannel), ReqChannel.CreateInternalChannel.class)
                        .retrieve()
                        .bodyToMono(ResponseWithData.class)
                        .block();
                if (responseWithData.getResponse().getOutput() != 0) {
                    log.error("google signup -> channel create error");
                }
            } else if (attributes.getRegistraionId().equalsIgnoreCase("facebook")) {
                dto = UserJoinDto.builder()
                        .mailId(attributes.getEmail())
                        .userName(attributes.getName())
                        .platform(attributes.getRegistraionId())
                        .privacyAgree("Y")
                        .userPicture(attributes.getPicture())
                        .oauth2Key(attributes.getAttributes().get("id").toString())
                        .googleOauth2Key(null)
                        .facebookOauth2Key(attributes.getAttributes().get("id").toString())
                        .appleOauth2Key(null)
                        .emailVerification("Y")
                        .build();
                log.info("first facebook login");
                log.info("sign up for facebook email : {}", dto);
                ReqChannel.CreateInternalChannel createInternalChannel = ReqChannel.CreateInternalChannel.builder()
                        .userNo(dto.getId())
                        .build();
                WebClient webClient = WebClient.builder()
                        .baseUrl(meitalkApiServer)
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .build();
                ResponseWithData responseWithData = webClient.post()
                        .uri(createChannelUrl)
                        .body(Mono.just(createInternalChannel), ReqChannel.CreateInternalChannel.class)
                        .retrieve()
                        .bodyToMono(ResponseWithData.class)
                        .block();
                if (responseWithData.getResponse().getOutput() != 0) {
                    log.error("facebook signup -> channel create error");
                }
            }
            userMapper.insertUserJoin(dto);
            user = userMapper.selectUserByEmail(attributes.getEmail());
            return user;
        } else {
            if (attributes.getRegistraionId().equalsIgnoreCase("google")) {
                log.info("oauth2 key update for google : {}, {}", user.getUserEmail(), user.getOauth2Key());
                userMapper.updateGoogleOauth2Key(user.getUserEmail(), attributes.getAttributes().get("sub").toString());
            } else if (attributes.getRegistraionId().equalsIgnoreCase("facebook")) {
                log.info("oauth2 key update for facebook : {}, {}", user.getUserEmail(), user.getOauth2Key());
                userMapper.updateFacebookOauth2Key(user.getUserEmail(), attributes.getAttributes().get("id").toString());
            }
            return user;
        }
    }
}
