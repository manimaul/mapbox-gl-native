package com.mapbox.android.core.location;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LocationEngineProviderTest {

  @Test(expected = NullPointerException.class)
  public void passNullContext() {
    LocationEngineProvider.getBestLocationEngine(null, false);
  }
}
