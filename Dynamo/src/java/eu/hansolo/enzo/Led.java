/*
 * Copyright (c) 2013 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.enzo;

import com.sun.javafx.css.converters.EnumConverter;
import com.sun.javafx.css.converters.PaintConverter;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.css.*;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import ve.marlontrujillo.scene.control.skin.LedSkin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;


/**
 * Created by
 * User: hansolo
 * Date: 16.11.12
 * Time: 09:19
 */
public class Led extends Control {
    public enum LedType {
        ROUND,
        SQUARE,
        VERTICAL,
        HORIZONTAL       
    }

    public static final String                STYLE_CLASS_RED     = "led-red";
    public static final String                STYLE_CLASS_GREEN   = "led-green";
    public static final String                STYLE_CLASS_BLUE    = "led-blue";
    public static final String                STYLE_CLASS_YELLOW  = "led-yellow";
    public static final String                STYLE_CLASS_ORANGE  = "led-orange";
    public static final String                STYLE_CLASS_CYAN    = "led-cyan";
    public static final String                STYLE_CLASS_MAGENTA = "led-magenta";
    public static final String                STYLE_CLASS_PURPLE  = "led-purple";
    public static final String                STYLE_CLASS_GRAY    = "led-gray";

    public static final LedType               DEFAULT_LED_TYPE  = LedType.ROUND;
    public static final Color                 DEFAULT_LED_COLOR = Color.RED;

    private static final PseudoClass          ON_PSEUDO_CLASS = PseudoClass.getPseudoClass("on");

    // CSS pseudo classes
    private BooleanProperty                   on;

    // CSS styleable properties
    private ObjectProperty<Paint>             ledColor;
    private ObjectProperty<LedType>           ledType;

    // Properties
    private boolean                           _blinking = false;
    private BooleanProperty                   blinking;
    private boolean                           _frameVisible = true;
    private BooleanProperty                   frameVisible;
    private int                               _interval = 500;
    private          IntegerProperty          interval;
    private volatile ScheduledFuture<?>       periodicBlinkTask;
    private static   ScheduledExecutorService periodicBlinkExecutorService;


    // ******************** Constructors **************************************
    public Led() {
        getStyleClass().add("led");    
    }


    // ******************** Methods *******************************************
    public final boolean isOn() {
        return null == on ? false : on.get();
    }
    public final void setOn(final boolean ON) {
        onProperty().set(ON);
    }
    public final BooleanProperty onProperty() {
        if (null == on) {
            on = new BooleanPropertyBase(false) {
                @Override protected void invalidated() { pseudoClassStateChanged(ON_PSEUDO_CLASS, get()); }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "on"; }
            };
        }
        return on;
    }

    public final boolean isBlinking() {
        return null == blinking ? _blinking : blinking.get();
    }
    public final void setBlinking(final boolean BLINKING) {
        if (null == blinking) {
            _blinking = BLINKING;
        } else {
            blinking.set(BLINKING);
        }
        if (BLINKING) {
            scheduleBlinkTask();
        } else {
            stopTask(periodicBlinkTask);
            setOn(false);
        }
    }
    public final BooleanProperty blinkingProperty() {
        if (null == blinking) {
            blinking = new SimpleBooleanProperty(this, "blinking", _blinking);
        }
        return blinking;
    }

    public final int getInterval() {
        return null == interval ? _interval : interval.get();
    }
    public final void setInterval(final int INTERVAL) {
        if (null == interval) {
            _interval = clamp(50, 5000, INTERVAL);
        } else {
            interval.set(clamp(50, 5000, INTERVAL));
        }
    }
    public final ReadOnlyIntegerProperty intervalProperty() {
        if (null == interval) {
            interval = new SimpleIntegerProperty(this, "interval", _interval);
        }
        return interval;
    }

    public final boolean isFrameVisible() {
        return null == frameVisible ? _frameVisible : frameVisible.get();
    }
    public final void setFrameVisible(final boolean FRAME_VISIBLE) {
        if (null == frameVisible) {
            _frameVisible = FRAME_VISIBLE;
        } else {
            frameVisible.set(FRAME_VISIBLE);
        }
    }
    public final BooleanProperty frameVisibleProperty() {
        if (null == frameVisible) {
            frameVisible = new SimpleBooleanProperty(this, "frameVisible", _frameVisible);
        }
        return frameVisible;
    }


    // ******************** CSS Stylable Properties ***************************
    public final Paint getLedColor() {
        return null == ledColor ? DEFAULT_LED_COLOR : ledColor.get();
    }
    public final void setLedColor(Paint value) {
        ledColorProperty().set(value);
    }
    public final ObjectProperty<Paint> ledColorProperty() {
        if (null == ledColor) {
            ledColor = new StyleableObjectProperty<Paint>(DEFAULT_LED_COLOR) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.LED_COLOR; }
                @Override public Object getBean() { return Led.this; }
                @Override public String getName() { return "ledColor"; }
            };
        }
        return ledColor;
    }

    public final LedType getLedType() {
        return null == ledType ? DEFAULT_LED_TYPE : ledType.get();
    }
    public final void setLedType(final LedType TYPE) {
        ledTypeProperty().set(TYPE);
    }
    public final ObjectProperty<LedType> ledTypeProperty() {
        if (null == ledType) {
            ledType = new StyleableObjectProperty<LedType>(DEFAULT_LED_TYPE) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.LED_TYPE; }
                @Override public Object getBean() { return Led.this; }
                @Override public String getName() { return "ledType";}
            };
        }
        return ledType;
    }


    // ******************** Utility Methods ***********************************
    public static int clamp(final int MIN, final int MAX, final int VALUE) {
        if (VALUE < MIN) return MIN;
        if (VALUE > MAX) return MAX;
        return VALUE;
    }


    // ******************** Scheduled tasks ***********************************
    private synchronized static void enableBlinkExecutorService() {
        if (null == periodicBlinkExecutorService) {
            periodicBlinkExecutorService = new ScheduledThreadPoolExecutor(1, getThreadFactory("BlinkLCD", false));
        }
    }
    private synchronized void scheduleBlinkTask() {
        enableBlinkExecutorService();
        stopTask(periodicBlinkTask);
        periodicBlinkTask = periodicBlinkExecutorService.scheduleAtFixedRate(() -> 
            Platform.runLater(() -> setOn(!isOn())), 0, getInterval(), TimeUnit.MILLISECONDS);
    }
    private static ThreadFactory getThreadFactory(final String THREAD_NAME, final boolean IS_DAEMON) {
        return runnable -> {
            Thread thread = new Thread(runnable, THREAD_NAME);
            thread.setDaemon(IS_DAEMON);
            return thread;
        };
    }
    private void stopTask(ScheduledFuture<?> task) {
        if (null == task) return;

        task.cancel(true);
        task = null;
    }
    

    // ******************** Style related *************************************
    @Override protected Skin createDefaultSkin() {
        return new LedSkin(this);
    }

    @Override public String getUserAgentStylesheet() {
        return getClass().getResource("led.css").toExternalForm();
    }

    private static class StyleableProperties {
        private static final CssMetaData<Led, Paint> LED_COLOR =
            new CssMetaData<Led, Paint>("-led-color", PaintConverter.getInstance(), DEFAULT_LED_COLOR) {

                @Override public boolean isSettable(Led led) {
                    return null == led.ledColor || !led.ledColor.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(Led led) {
                    return (StyleableProperty) led.ledColorProperty();
                }

                @Override public Color getInitialValue(Led led) {
                    return (Color) led.getLedColor();
                }
            };

        private static final CssMetaData<Led, LedType> LED_TYPE =
            new CssMetaData<Led, LedType>("-led-type", new EnumConverter<>(LedType.class)) {

                @Override public boolean isSettable(Led led) {
                    return null == led.ledType || !led.ledType.isBound();
                }

                @Override public StyleableProperty<LedType> getStyleableProperty(Led led) {
                    return (StyleableProperty) led.ledTypeProperty();
                }

                @Override public LedType getInitialValue(Led led) {
                    return led.getLedType();
                }
            };


        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            Collections.addAll(styleables,
                LED_COLOR,
                LED_TYPE
            );
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }
}
