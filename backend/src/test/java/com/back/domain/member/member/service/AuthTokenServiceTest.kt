package com.back.domain.member.member.service

import com.back.standard.util.Ut
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthTokenServiceTest(
    @Autowired private val memberService: MemberService,
    @Autowired private val authTokenService: AuthTokenService,
    @Value("\${custom.jwt.secretKey}") private val jwtSecretKey: String,
    @Value("\${custom.accessToken.expirationSeconds}") private val accessTokenExpirationSeconds: Int
) {

    @Test
    @DisplayName("authTokenService 서비스가 존재한다.")
    fun `서비스가 존재한다`() {
        assertThat(authTokenService).isNotNull
    }

    @Test
    @DisplayName("jjwt 최신 방식으로 JWT 생성, {name=\"Paul\", age=23}")
    fun `JJWT 최신 방식으로 JWT 생성`() {
        // Given
        val expireMillis = 1000L * accessTokenExpirationSeconds
        val keyBytes = jwtSecretKey.toByteArray(StandardCharsets.UTF_8)
        val secretKey = Keys.hmacShaKeyFor(keyBytes)
        
        val issuedAt = Date()
        val expiration = Date(issuedAt.time + expireMillis)
        val payload = mapOf(
            "name" to "Paul",
            "age" to 23
        )

        // When
        val jwt = Jwts.builder()
            .claims(payload)
            .issuedAt(issuedAt)
            .expiration(expiration)
            .signWith(secretKey)
            .compact()

        // Then
        assertThat(jwt).isNotBlank
        println("jwt = $jwt")

        val parsedPayload = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parse(jwt)
            .payload as Map<String, Any>

        assertThat(parsedPayload).containsAllEntriesOf(payload)
    }

    @Test
    @DisplayName("Ut.jwt.toString 를 통해서 JWT 생성, {name=\"Paul\", age=23}")
    fun `Ut jwt toString으로 JWT 생성`() {
        // Given
        val payload = mapOf("name" to "Paul", "age" to 23)

        // When
        val jwt = Ut.jwt.toString(jwtSecretKey, accessTokenExpirationSeconds, payload)

        // Then
        assertThat(jwt).isNotBlank
        assertThat(Ut.jwt.isValid(jwtSecretKey, jwt)).isTrue

        val parsedPayload = Ut.jwt.payload(jwtSecretKey, jwt)
        assertThat(parsedPayload).containsAllEntriesOf(payload)
    }

    @Test
    @DisplayName("authTokenService.genAccessToken(member)")
    fun `사용자 액세스 토큰 생성`() {
        // Given
        val memberUser1 = memberService.findByUsername("user1").get()

        // When
        val accessToken = authTokenService.genAccessToken(memberUser1)

        // Then
        assertThat(accessToken).isNotBlank
        println("accessToken = $accessToken")

        val parsedPayload = authTokenService.payload(accessToken)
        assertThat(parsedPayload).containsAllEntriesOf(
            mapOf(
                "id" to memberUser1.id,
                "username" to memberUser1.username,
                "name" to memberUser1.name
            )
        )
    }
}
