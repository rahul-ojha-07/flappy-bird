package com.example.utils;


import java.net.URL;

public class FileUtils{
    public static URL getResourceFile(final Object obj, final String fileName)
    {
        URL url = obj.getClass()
                .getClassLoader()
                .getResource(fileName);

        if(url == null) {
            throw new IllegalArgumentException(fileName + " is not found 1");
        }
        return url;
    }
}