package com.mapbox.mapboxsdk.provider;

import org.junit.Before;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class OfflineProviderManagerTest {

    OfflineProviderManager _offlineProviderManager;

    @Before
    public void setup() {
        _offlineProviderManager = OfflineProviderManager.getInstance(getResources(), database);
    }

//    @Test
//    public void testWillHandleUrl() throws Exception {
//        OfflineProvider provider = mock(OfflineProvider.class);
//        _offlineProviderManager.registerProvider(provider);
//
//        _offlineProviderManager.mHost = "localhost";
//        assertTrue(_offlineProviderManager.willHandleUrl("http://localhost/8/40/91"));
//        assertTrue(_offlineProviderManager.willHandleUrl("https://localhost/8/40/91"));
//        assertFalse(_offlineProviderManager.willHandleUrl("http://mapbox.com/8/40/91"));
//
//        String random = UUID.randomUUID().toString();
//
//        _offlineProviderManager.mHost = String.format("localhost_%s", random);
//        String url = String.format("http://%s/8/40/91", _offlineProviderManager.mHost);
//        assertTrue(_offlineProviderManager.willHandleUrl(url));
//
//        url = String.format("https://%s/8/40/91", _offlineProviderManager.mHost);
//        assertTrue(_offlineProviderManager.willHandleUrl(url));
//
//        url = String.format("ftp://%s/8/40/91", _offlineProviderManager.mHost);
//        assertTrue(_offlineProviderManager.willHandleUrl(url));
//    }
//    @Test
//    public void testWillHandleUrl_NoProvider() throws Exception {
//        _offlineProviderManager.mProvider = null;
//        assertFalse(_offlineProviderManager.willHandleUrl("http://localhost/8/40/91"));
//        assertFalse(_offlineProviderManager.willHandleUrl("https://localhost/8/40/91"));
//        assertFalse(_offlineProviderManager.willHandleUrl("http://mapbox.com/8/40/91"));
//
//        String random = UUID.randomUUID().toString();
//
//        String url = String.format("http://localhost_%s/8/40/91", random);
//        assertFalse(_offlineProviderManager.willHandleUrl(url));
//
//        url = String.format("https://localhost_%s/8/40/91", random);
//        assertFalse(_offlineProviderManager.willHandleUrl(url));
//
//        url = String.format("ftp://localhost_%s/8/40/91", random);
//        assertFalse(_offlineProviderManager.willHandleUrl(url));
//    }
//
//    @Test
//    public void testGetUrlHost() throws Exception {
//        assertEquals(OfflineProviderManager.LOCALHOST,
//                _offlineProviderManager.getUrlHost("http://localhost/8/40/91"));
//
//        assertEquals(OfflineProviderManager.LOCALHOST,
//                _offlineProviderManager.getUrlHost("http://localhost/"));
//
//        assertEquals(OfflineProviderManager.LOCALHOST,
//                _offlineProviderManager.getUrlHost("https://localhost/8/40/91"));
//
//        assertEquals(OfflineProviderManager.LOCALHOST,
//                _offlineProviderManager.getUrlHost("ftp://localhost/8/40/91"));
//    }
//
//    @Test
//    public void testHandleRequest() throws Exception {
//        OfflineProvider provider = mock(OfflineProvider.class);
//        HTTPRequest mockRequest = mock(HTTPRequest.class);
//        _offlineProviderManager.registerProvider(provider);
//
//        String url = String.format("http://%s/1/2/3", _offlineProviderManager.mHost);
//        _offlineProviderManager.handleRequest(mockRequest, url);
//        verify(provider).startFetchForTile(eq(1), eq(2), eq(3),
//                any(OfflineProviderCallback.class));
//
//        url = String.format("https://%s/4/5/6", _offlineProviderManager.mHost);
//        _offlineProviderManager.handleRequest(mockRequest, url);
//        verify(provider).startFetchForTile(eq(4), eq(5), eq(6),
//                any(OfflineProviderCallback.class));
//
//        url = String.format("ftp://%s/7/8/9", _offlineProviderManager.mHost);
//        _offlineProviderManager.handleRequest(mockRequest, url);
//        verify(provider).startFetchForTile(eq(7), eq(8), eq(9),
//                any(OfflineProviderCallback.class));
//
//    }
}