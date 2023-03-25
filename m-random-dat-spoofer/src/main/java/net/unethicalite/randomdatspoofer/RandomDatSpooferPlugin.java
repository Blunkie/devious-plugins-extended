/*
 * Copyright (c) 2022, Melxin <https://github.com/melxin/>,
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.unethicalite.randomdatspoofer;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

@Extension
@PluginDescriptor(
        name = "mRandom dat spoofer",
        description = "Spoofs the random.dat file",
        enabledByDefault = false,
        tags =
                {
                        "random.dat",
                        "spoof",
                        "runelite"
                }
)
@Slf4j
public class RandomDatSpooferPlugin extends Plugin
{
    private final File jagexCache = new File(com.openosrs.client.OpenOSRS.OPENOSRS_DIR, "jagexcache");
    private final File randomDatFile = new File(jagexCache + File.separator + "random.dat");
    private final byte[] rlLoginBlock = new byte[] {-35, -91, -50, -47, -12, 24, 52, -102, 123, -32, 118, 92, -77, 48, 100, 12, -52, -79, -106, 34, -102, 91, -19, 121};

    /**
     * Spoof the random.dat file with RuneLite's login block
     *
     * i noticed that when you use a different client
     * that the bytes stored in random.dat differs from RuneLite
     *
     * i am not sure if this actually works but it might prevent third-party client detection
     * and/or multiple accounts being flagged at the same time
     */
    private void spoofRandomDat()
    {
        try
        {
            if (!randomDatFile.exists())
            {
                randomDatFile.createNewFile();
            }

            byte[] currentLoginBlock = Files.readAllBytes(randomDatFile.toPath());
            log.info("Current login block: {}", currentLoginBlock);
            if (Arrays.equals(currentLoginBlock, rlLoginBlock))
            {
                log.info("{} is successfully spoofed!", randomDatFile.getAbsolutePath());
                return;
            }

            Files.write(randomDatFile.toPath(), rlLoginBlock, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Successfully wrote rl login block {} to {}", rlLoginBlock, randomDatFile.getAbsolutePath());
        }
        catch (IOException e)
        {
            log.error("Failed to write rl login block data to: {}", randomDatFile, e);
        }
    }

    @Override
    protected void startUp()
    {
        spoofRandomDat();
    }

    @Override
    protected void shutDown()
    {
        try
        {
            Files.delete(randomDatFile.toPath());
            log.info("Successfully deleted: {}", randomDatFile.getAbsolutePath());
        }
        catch (IOException e)
        {
            log.error("Failed to delete: {}", randomDatFile.getAbsolutePath(), e);
        }
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGIN_SCREEN)
        {
            spoofRandomDat();
        }
    }
}
