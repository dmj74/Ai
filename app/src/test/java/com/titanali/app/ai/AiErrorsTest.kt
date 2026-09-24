package com.titanali.app.ai

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import org.junit.Assert.assertEquals
import org.junit.Test

class AiErrorsTest {

    @Test
    fun `http statuses map to stable keys`() {
        assertEquals(AiErrors.AUTH, AiErrors.keyForStatus(401))
        assertEquals(AiErrors.AUTH, AiErrors.keyForStatus(403))
        assertEquals(AiErrors.MODEL, AiErrors.keyForStatus(404))
        assertEquals(AiErrors.RATE, AiErrors.keyForStatus(429))
        assertEquals(AiErrors.SERVER, AiErrors.keyForStatus(503))
        assertEquals("http:418", AiErrors.keyForStatus(418))
    }

    @Test
    fun `exceptions map to stable keys`() {
        assertEquals(AiErrors.AUTH, AiErrors.keyFor(AiHttpException(401, "bad key")))
        assertEquals(AiErrors.TIMEOUT, AiErrors.keyFor(SocketTimeoutException("t")))
        assertEquals(AiErrors.NETWORK, AiErrors.keyFor(UnknownHostException("x")))
        assertEquals(AiErrors.NETWORK, AiErrors.keyFor(IOException("x")))
        assertEquals(AiErrors.EMPTY, AiErrors.keyFor(AiEmptyReplyException()))
        assertEquals(AiErrors.UNKNOWN, AiErrors.keyFor(RuntimeException("x")))
    }
}
