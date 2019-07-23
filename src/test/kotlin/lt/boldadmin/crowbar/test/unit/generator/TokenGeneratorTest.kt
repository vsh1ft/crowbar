package lt.boldadmin.crowbar.test.unit.generator

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import lt.boldadmin.crowbar.generator.TokenGenerator
import lt.boldadmin.crowbar.repository.UserTokenRepository
import lt.boldadmin.crowbar.type.entity.UserToken
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class TokenGeneratorTest {

    @MockK
    private lateinit var repositoryMock: UserTokenRepository

    private lateinit var tokenGenerator: TokenGenerator

    @BeforeEach
    fun `Set up`() {
        tokenGenerator = TokenGenerator(repositoryMock)
    }

    @Test
    fun `Stores authentication token`() {
        every { repositoryMock.existsByToken(any()) } returns false
        every { repositoryMock.save(any()) } just Runs

        tokenGenerator.generateAndStore(USER_ID)

        verify { repositoryMock.save(any()) }
    }

    @Test
    fun `Generates authentication token`() {
        val capturedToken = CapturingSlot<String>()
        every { repositoryMock.existsByToken(capture(capturedToken)) } returns false
        every { repositoryMock.save(any()) } just Runs

        tokenGenerator.generateAndStore(USER_ID)

        assertTrue(capturedToken.captured.isNotEmpty())
    }

    @Test
    fun `Generates authentication token until unique one is found`() {
        val tokens = mutableListOf<String>()
        val savedToken = CapturingSlot<UserToken>()
        every { repositoryMock.existsByToken(capture(tokens)) } returns true andThen false
        every { repositoryMock.save(capture(savedToken)) } just Runs

        tokenGenerator.generateAndStore(USER_ID)

        assertTrue(tokens[0] != tokens[1])
        assertEquals(USER_ID, savedToken.captured.id)
        assertEquals(tokens[1], savedToken.captured.token)
    }

    @Test
    fun `Checks if token exists`() {
        every { repositoryMock.existsByToken(TOKEN) } returns true

        assertTrue(tokenGenerator.doesTokenExist(TOKEN))
    }

    @Test
    fun `Gets token by user id`() {
        every { repositoryMock.findById(USER_ID) } returns Optional.of(UserToken(USER_ID, TOKEN))

        val responseToken = tokenGenerator.getTokenById(USER_ID)

        assertSame(TOKEN, responseToken)
    }

    @Test
    fun `Gets user id by token`() {
        every { repositoryMock.findByToken(TOKEN) } returns UserToken(USER_ID, TOKEN)

        val responseId = tokenGenerator.getUserIdByToken(TOKEN)

        assertSame(USER_ID, responseId)
    }

    companion object {
        private val USER_ID = "userId"
        private val TOKEN = "token"
    }
}
