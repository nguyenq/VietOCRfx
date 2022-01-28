/**
 * Copyright @ 2022 Quan Nguyen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.vietocr;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TesseractParameters {

    private final StringProperty datapath = new SimpleStringProperty("./");

    public final String getDatapath() {
        return datapath.get();
    }

    public final void setDatapath(String value) {
        datapath.set(value);
    }

    public StringProperty datapathProperty() {
        return datapath;
    }
    
    private final StringProperty langCode = new SimpleStringProperty("eng");

    public final String getLangCode() {
        return langCode.get();
    }

    public final void setLangCode(String value) {
        langCode.set(value);
    }

    public StringProperty langCodeProperty() {
        return langCode;
    }
    
    private final StringProperty psm = new SimpleStringProperty("3"); // 3 - Fully automatic page segmentation, but no OSD (default)

    public final String getPsm() {
        return psm.get();
    }

    public final void setPsm(String value) {
        psm.set(value);
    }

    public StringProperty psmProperty() {
        return psm;
    }
}
