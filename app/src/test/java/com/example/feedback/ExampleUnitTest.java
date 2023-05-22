package com.example.feedback;

import org.junit.Test;

import static org.junit.Assert.*;

import com.example.feedback.DataClasses.User;

import java.util.ArrayList;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        ArrayList<User> users = new ArrayList<>();
        users.add(new User("Yaron Ben Yehuda", "yby"));
        users.add(new User("Andy Bekerman", "ab"));
        users.add(new User("Shlomi Dabush", "sd"));
        users.stream().filter(u -> u.getFullName().contains("y")).count();
        assertEquals(3, 2 + 2);
    }
}