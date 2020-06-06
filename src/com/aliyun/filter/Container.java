package com.aliyun.filter;

/**
 * Page容器，主要负责管理Page
 */
public class Container {
    public static final int len = 11;
    private static Page[] emptyPages = new Page[11];
    private static Page[] fullPages = new Page[11];

    static {
        for (int i = 0; i < emptyPages.length; i++) {
            emptyPages[i] = new Page();
        }
    }


    public static synchronized Page getEmptyPage(int i) {
        Page page = emptyPages[i % len];
        while (page == null) {
            try {
                Container.class.wait();
                page = emptyPages[i % len];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return page;
    }

    public static synchronized Page getFullPage(int i) {
        Page page = fullPages[i % len];
        while (page == null) {
            try {
                Container.class.wait();
                page = fullPages[i % len];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return page;
    }

    public static synchronized void movePageToEmpty(int i) {
        try {
            emptyPages[i % len] = fullPages[i % len];
            Container.class.notifyAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void movePageToFull(int i) {
        try {
            fullPages[i % len] = emptyPages[i % len];
            Container.class.notifyAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
