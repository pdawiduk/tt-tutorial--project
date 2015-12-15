package com.example.dawiduk.podejscie2;

import android.content.Context;
import android.widget.ArrayAdapter;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.Assertions.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 18)
public class BackgroundTaskTest {

    @Mock
    ArrayAdapter mockAdapter;
    @Mock
    Context mockContext;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldFormatLongTime() throws Exception {
        //GIVEN background task and time in long type
        BackgroundTask testedObj = new BackgroundTask(mockAdapter, mockContext);
        long time = 0; // 1 JAN 1970
        String expected = "Thu Jan 01";//EEE MMM dd
        //WHEN formatting time
        String result =  testedObj.getReadableDateString(0);
        //THEN correct date
        assertThat(result).isEqualTo(expected);
    }
}