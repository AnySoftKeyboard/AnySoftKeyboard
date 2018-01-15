/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.base.utils;

public interface LogProvider {

    boolean supportsV();

    void v(String tag, String text);

    boolean supportsD();

    void d(String tag, String text);

    boolean supportsYell();

    void yell(String tag, String text);

    boolean supportsI();

    void i(String tag, String text);

    boolean supportsW();

    void w(String tag, String text);

    boolean supportsE();

    void e(String tag, String text);

    boolean supportsWTF();

    void wtf(String tag, String text);
}
