package com.habitflow.util;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * AnimationHelper — reusable animations
 * for the entire app.
 *
 * Fade-in, slide-in, scale-pop, etc.
 */
public class AnimationHelper {

    // ═══════════════════════════════════════
    // FADE IN
    // ═══════════════════════════════════════

    /**
     * Fades a node from 0 to 1 opacity.
     */
    public static void fadeIn(
            Node node, double millis) {
        node.setOpacity(0);
        FadeTransition ft =
            new FadeTransition(
                Duration.millis(millis), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setInterpolator(
            Interpolator.EASE_OUT);
        ft.play();
    }

    /**
     * Fades in with a slide up effect.
     */
    public static void fadeSlideIn(
            Node node, double millis,
            double slideDistance) {
        node.setOpacity(0);
        node.setTranslateY(slideDistance);

        FadeTransition fade =
            new FadeTransition(
                Duration.millis(millis), node);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide =
            new TranslateTransition(
                Duration.millis(millis), node);
        slide.setFromY(slideDistance);
        slide.setToY(0);
        slide.setInterpolator(
            Interpolator.EASE_OUT);

        new ParallelTransition(fade, slide)
            .play();
    }

    // ═══════════════════════════════════════
    // SLIDE IN FROM RIGHT
    // ═══════════════════════════════════════

    /**
     * Slides a node in from the right
     * with fade.
     */
    public static void slideInFromRight(
            Node node, double millis,
            double delay) {
        node.setOpacity(0);
        node.setTranslateX(40);

        FadeTransition fade =
            new FadeTransition(
                Duration.millis(millis), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(
            Duration.millis(delay));

        TranslateTransition slide =
            new TranslateTransition(
                Duration.millis(millis), node);
        slide.setFromX(40);
        slide.setToX(0);
        slide.setDelay(
            Duration.millis(delay));
        slide.setInterpolator(
            Interpolator.EASE_OUT);

        new ParallelTransition(fade, slide)
            .play();
    }

    // ═══════════════════════════════════════
    // SCALE POP
    // ═══════════════════════════════════════

    /**
     * Pops a node with a quick scale effect.
     */
    public static void scalePop(
            Node node, double millis) {
        ScaleTransition st =
            new ScaleTransition(
                Duration.millis(millis), node);
        st.setFromX(0.9);
        st.setFromY(0.9);
        st.setToX(1.0);
        st.setToY(1.0);
        st.setInterpolator(
            Interpolator.EASE_OUT);
        st.play();
    }

    // ═══════════════════════════════════════
    // HOVER SCALE (attach to node)
    // ═══════════════════════════════════════

    /**
     * Adds a hover scale effect to a node.
     * Uses addEventHandler so it doesn't
     * overwrite existing mouse handlers.
     */
    public static void addHoverScale(
            Node node, double scale) {
        node.addEventHandler(
            javafx.scene.input.MouseEvent
                .MOUSE_ENTERED,
            e -> {
                ScaleTransition grow =
                    new ScaleTransition(
                    Duration.millis(120), node);
                grow.setToX(scale);
                grow.setToY(scale);
                grow.setInterpolator(
                    Interpolator.EASE_OUT);
                grow.play();
            });

        node.addEventHandler(
            javafx.scene.input.MouseEvent
                .MOUSE_EXITED,
            e -> {
                ScaleTransition shrink =
                    new ScaleTransition(
                    Duration.millis(120), node);
                shrink.setToX(1.0);
                shrink.setToY(1.0);
                shrink.setInterpolator(
                    Interpolator.EASE_OUT);
                shrink.play();
            });
    }

    // ═══════════════════════════════════════
    // COUNT UP (for stat numbers)
    // ═══════════════════════════════════════

    /**
     * Animates a label counting up from 0
     * to the target value.
     */
    public static void countUp(
            javafx.scene.control.Label label,
            int target, String suffix,
            double millis) {

        if (target == 0) {
            label.setText("0" + suffix);
            return;
        }

        Timeline timeline = new Timeline();
        int frames = 20;
        for (int i = 0; i <= frames; i++) {
            final int val =
                (int) Math.round(
                    target * (i / (double) frames));
            KeyFrame kf = new KeyFrame(
                Duration.millis(
                    millis * i / frames),
                e -> label.setText(
                    val + suffix));
            timeline.getKeyFrames().add(kf);
        }
        timeline.play();
    }

    // ═══════════════════════════════════════
    // CHART ANIMATIONS
    // ═══════════════════════════════════════

    /**
     * Fades and scales a chart in.
     */
    public static void chartEntrance(
            Node chart, double delay) {
        chart.setOpacity(0);
        chart.setScaleX(0.92);
        chart.setScaleY(0.92);

        FadeTransition fade =
            new FadeTransition(
                Duration.millis(500), chart);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(
            Duration.millis(delay));

        ScaleTransition scale =
            new ScaleTransition(
                Duration.millis(500), chart);
        scale.setFromX(0.92);
        scale.setFromY(0.92);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setDelay(
            Duration.millis(delay));
        scale.setInterpolator(
            Interpolator.EASE_OUT);

        new ParallelTransition(fade, scale)
            .play();
    }

    // ═══════════════════════════════════════
    // THEME CROSSFADE
    // ═══════════════════════════════════════

    /**
     * Smooth crossfade for theme transitions.
     * Fades out, applies change, fades back in.
     */
    public static void themeCrossfade(
            Node root, Runnable applyTheme) {

        FadeTransition fadeOut =
            new FadeTransition(
                Duration.millis(150), root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0.3);
        fadeOut.setOnFinished(e -> {
            applyTheme.run();
            FadeTransition fadeIn =
                new FadeTransition(
                    Duration.millis(250), root);
            fadeIn.setFromValue(0.3);
            fadeIn.setToValue(1);
            fadeIn.play();
        });
        fadeOut.play();
    }
}