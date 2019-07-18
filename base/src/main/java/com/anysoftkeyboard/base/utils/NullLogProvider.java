package com.anysoftkeyboard.base.utils;

/** Doesn't do anything. For release. */
public class NullLogProvider implements LogProvider {

    @Override
    public boolean supportsV() {
        return false;
    }

    @Override
    public void v(String tag, String text) {}

    @Override
    public boolean supportsD() {
        return false;
    }

    @Override
    public void d(String tag, String text) {}

    @Override
    public boolean supportsYell() {
        return false;
    }

    @Override
    public void yell(String tag, String text) {}

    @Override
    public boolean supportsI() {
        return false;
    }

    @Override
    public void i(String tag, String text) {}

    @Override
    public boolean supportsW() {
        return false;
    }

    @Override
    public void w(String tag, String text) {}

    @Override
    public boolean supportsE() {
        return false;
    }

    @Override
    public void e(String tag, String text) {}

    @Override
    public boolean supportsWTF() {
        return false;
    }

    @Override
    public void wtf(String tag, String text) {}
}
