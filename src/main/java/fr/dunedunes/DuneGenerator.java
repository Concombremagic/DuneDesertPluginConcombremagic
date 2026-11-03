package fr.dunedunes;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.HeightMap;
import org.bukkit.Material;

import java.util.List;
import java.util.Random;

public final class DuneGenerator extends ChunkGenerator {
    private static final BiomeProvider DESERT_BIOMES = new BiomeProvider() {
        @Override
        public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
            return Biome.DESERT;
        }

        @Override
        public List<Biome> getBiomes(WorldInfo worldInfo) {
            return List.of(Biome.DESERT);
        }
    };

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        return DESERT_BIOMES;
    }

    // Le générateur construit lui-même le terrain : pas de bruit vanilla.
    @Override public boolean shouldGenerateNoise() { return false; }
    @Override public boolean shouldGenerateSurface() { return false; }
    @Override public boolean shouldGenerateCaves() { return false; }
    @Override public boolean shouldGenerateDecorations() { return false; }
    @Override public boolean shouldGenerateStructures() { return false; }
    @Override public boolean shouldGenerateMobs() { return true; }

    @Override
    public void generateNoise(WorldInfo info, Random random, int chunkX, int chunkZ, ChunkData data) {
        final long seed = info.getSeed();
        final int minY = data.getMinHeight();
        final int maxY = data.getMaxHeight();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;

                int surfaceY = duneHeight(seed, worldX, worldZ);
                surfaceY = Math.max(minY + 8, Math.min(maxY - 8, surfaceY));

                // Fond solide en grès : aucune cavité ni minerai.
                data.setBlock(x, minY, z, Material.BEDROCK);
                for (int y = minY + 1; y <= surfaceY; y++) {
                    int depth = surfaceY - y;
                    if (depth < 4) {
                        data.setBlock(x, y, z, surfaceMaterial(seed, worldX, y, worldZ));
                    } else {
                        data.setBlock(x, y, z, Material.SANDSTONE);
                    }
                }
            }
        }
    }

    @Override
    public int getBaseHeight(WorldInfo info, Random random, int x, int z, HeightMap heightMap) {
        return duneHeight(info.getSeed(), x, z) + 1;
    }

    // Grandes ondulations douces : dunes larges, vallées, sans pics montagneux.
    private static int duneHeight(long seed, int x, int z) {
        double broad = valueNoise(seed, x * 0.0030, z * 0.0030);
        double medium = valueNoise(seed + 1013L, x * 0.0080, z * 0.0080);
        double fine = valueNoise(seed + 7919L, x * 0.0200, z * 0.0200);

        double height = 72.0 + broad * 10.0 + medium * 4.0 + fine * 1.2;
        return (int) Math.round(height);
    }

    // Mélange indépendant bloc par bloc : 50/20/20/10.
    private static Material surfaceMaterial(long seed, int x, int y, int z) {
        double r = unitHash(seed, x, y, z);
        if (r < 0.50) return Material.SAND;
        if (r < 0.70) return Material.SANDSTONE;
        if (r < 0.90) return Material.SUSPICIOUS_SAND;
        return Material.END_STONE;
    }

    private static double valueNoise(long seed, double x, double z) {
        int x0 = (int) Math.floor(x);
        int z0 = (int) Math.floor(z);
        int x1 = x0 + 1;
        int z1 = z0 + 1;
        double tx = fade(x - x0);
        double tz = fade(z - z0);

        double a = lerp(hashNoise(seed, x0, z0), hashNoise(seed, x1, z0), tx);
        double b = lerp(hashNoise(seed, x0, z1), hashNoise(seed, x1, z1), tx);
        return lerp(a, b, tz);
    }

    private static double hashNoise(long seed, int x, int z) {
        long h = seed;
        h ^= (long) x * 0x9E3779B97F4A7C15L;
        h ^= (long) z * 0xC2B2AE3D27D4EB4FL;
        h = mix64(h);
        return ((h >>> 11) * 0x1.0p-52) - 1.0;
    }

    private static double unitHash(long seed, int x, int y, int z) {
        long h = seed ^ ((long) x * 0x9E3779B97F4A7C15L)
                      ^ ((long) y * 0xC2B2AE3D27D4EB4FL)
                      ^ ((long) z * 0x165667B19E3779F9L);
        h = mix64(h);
        return (h >>> 11) * 0x1.0p-53;
    }

    private static long mix64(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
