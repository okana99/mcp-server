package net.portswigger.mcp.security

import burp.api.montoya.logging.Logging
import burp.api.montoya.persistence.PersistedObject
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import net.portswigger.mcp.config.McpConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DataAccessSecurityTest {
    private lateinit var originalApprovalHandler: DataAccessApprovalHandler
    private lateinit var approvalHandler: DataAccessApprovalHandler
    private lateinit var config: McpConfig

    @BeforeEach
    fun setup() {
        originalApprovalHandler = DataAccessSecurity.approvalHandler
        approvalHandler = mockk()
        DataAccessSecurity.approvalHandler = approvalHandler

        val storage = mutableMapOf<String, Any>(
            "requireDataAccessApproval" to true,
            "_alwaysAllowHttpHistory" to true,
            "_alwaysAllowWebSocketHistory" to true,
            "_alwaysAllowOrganizer" to true,
        )
        val persistedObject = mockk<PersistedObject>().apply {
            every { getBoolean(any()) } answers { storage[firstArg()] as? Boolean }
            every { getString(any()) } answers { storage[firstArg()] as? String }
            every { getInteger(any()) } answers { storage[firstArg()] as? Int }
            every { setBoolean(any(), any()) } answers { storage[firstArg()] = secondArg<Boolean>() }
            every { setString(any(), any()) } answers { storage[firstArg()] = secondArg<String>() }
            every { setInteger(any(), any()) } answers { storage[firstArg()] = secondArg<Int>() }
        }
        config = McpConfig(persistedObject, mockk<Logging>(relaxed = true))
    }

    @AfterEach
    fun tearDown() {
        DataAccessSecurity.approvalHandler = originalApprovalHandler
    }

    @Test
    fun `full agent data settings should allow every data type without a dialog`() = runBlocking {
        DataAccessType.entries.forEach { accessType ->
            assertTrue(DataAccessSecurity.checkDataAccessPermission(accessType, config))
        }

        coVerify(exactly = 0) { approvalHandler.requestDataAccess(any(), any()) }
    }
}
