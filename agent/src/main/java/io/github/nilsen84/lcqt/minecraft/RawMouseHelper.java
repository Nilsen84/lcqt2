package io.github.nilsen84.lcqt.minecraft;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.DirectAndRawInputEnvironmentPlugin;
import net.java.games.input.Mouse;
import net.minecraft.util.MouseHelper;

import java.util.Arrays;

public class RawMouseHelper extends MouseHelper {
    ControllerEnvironment enviro = new DirectAndRawInputEnvironmentPlugin();

    Mouse[] mice = Arrays.stream(enviro.getControllers())
            .filter(c -> c.getType() == Controller.Type.MOUSE)
            .map(Mouse.class::cast)
            .toArray(Mouse[]::new);

    @Override
    public void mouseXYChange() {
        this.deltaX = 0;
        this.deltaY = 0;

        for(var mouse : mice) {
            mouse.poll();
            this.deltaX += mouse.getX().getPollData();
            this.deltaY -= mouse.getY().getPollData();
        }
    }
}
