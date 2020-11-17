package com.example.frontend;

import com.example.frontend.ui.home.HomeFragment;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class OutfitResponseTimeTest {
    @Test
    public void responseTime_lessThan2Sec(){
        System.out.println("Start a new test!");
        String testUserToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiI1ZmIyY2FhZDk5ZDM1NzI1YzQyN2M3YWUiLCJlbWFpbCI6Im05dGVzdEBtOXRlc3QuY29tIiwiaWF0IjoxNjA1NTYzNTA4LCJleHAiOjE2MDU1NjcxMDh9.z0r9bU7bbLC-HmY1PznEtKwbA4PnE7pXLDfbyXLl33E";

        HomeFragment homeFragment = new HomeFragment();
        long startTime = System.currentTimeMillis();
        System.out.println("The timestamp before sending request to get an outfit recommendation is: "+startTime+"ms");
        homeFragment.getOutfitData(testUserToken);
        long endTime = System.currentTimeMillis();
        System.out.println("The timestamp when receiving server's response is: "+endTime+"ms");
        long elapsedTime = endTime - startTime;

        assertTrue(elapsedTime<=2000);
        System.out.println("Response time to server endpoint /api/outfits/one/"+" is: " + elapsedTime+ "ms"+" which is be less than expected 2000ms");
    }
}