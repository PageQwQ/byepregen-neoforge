package com.moepus.byepregen;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

public final class MixinPlugin implements IMixinConfigPlugin {
    private static final String MIXIN_PACKAGE = "com.moepus.byepregen.mixin.";
    private static final String YA_LIGHT_MIXIN_PREFIX = MIXIN_PACKAGE + "yalight.";

    private static final String CHUNK_ACCESS_ARENA_MIXIN = MIXIN_PACKAGE + "ChunkAccessArenaMixin";
    private static final String CHUNK_SERIALIZER_ARENA_READ_MIXIN =
            MIXIN_PACKAGE + "PalettedContainerFactoryArenaMixin";
    private static final String LEVEL_CHUNK_ARENA_MIXIN = MIXIN_PACKAGE + "LevelChunkArenaMixin";
    private static final String NOISE_CHUNK_ACCESSOR = MIXIN_PACKAGE + "NoiseChunkAccessor";
    private static final String NOISE_CHUNK_ARENA_MIXIN = MIXIN_PACKAGE + "NoiseChunkArenaMixin";
    private static final String NOISE_CELL_CACHE_ARENA_MIXIN =
            MIXIN_PACKAGE + "NoiseChunkCellCacheArenaMixin";
    private static final String NOISE_INTERPOLATOR_ARENA_MIXIN =
            MIXIN_PACKAGE + "NoiseInterpolatorArenaMixin";
    private static final String NOISE_GENERATOR_ARENA_MIXIN =
            MIXIN_PACKAGE + "NoiseBasedChunkGeneratorArenaMixin";
    private static final String VOXY_ARENA_MIXIN =
            MIXIN_PACKAGE + "compat.VoxyWorldConversionFactoryMixin";

    private static final Set<String> ARENA_MIXINS = Set.of(
            CHUNK_ACCESS_ARENA_MIXIN,
            CHUNK_SERIALIZER_ARENA_READ_MIXIN,
            LEVEL_CHUNK_ARENA_MIXIN,
            NOISE_CHUNK_ACCESSOR,
            NOISE_CHUNK_ARENA_MIXIN,
            NOISE_CELL_CACHE_ARENA_MIXIN,
            NOISE_INTERPOLATOR_ARENA_MIXIN,
            NOISE_GENERATOR_ARENA_MIXIN,
            VOXY_ARENA_MIXIN
    );

    private static final MixinGateEvaluator MIXIN_GATE_EVALUATOR = MixinGateEvaluator.createDefault();

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        Config config = ConfigParser.getConfig();
        boolean featureEnabled = passesFeatureGate(mixinClassName, config, MixinPlugin::hasClass);
        boolean annotationEnabled = MIXIN_GATE_EVALUATOR.shouldApply(targetClassName, mixinClassName, config);
        return featureEnabled && annotationEnabled;
    }

    static boolean passesFeatureGate(
            String mixinClassName,
            Config config,
            Predicate<String> classExists
    ) {
        if (mixinClassName.startsWith(YA_LIGHT_MIXIN_PREFIX)) {
            return config.enableYALightEngine;
        }
        if (ARENA_MIXINS.contains(mixinClassName)) {
            return passesArenaGate(mixinClassName, config);
        }
        return true;
    }

    private static boolean passesArenaGate(String mixinClassName, Config config) {
        if (!config.enableArenaPalette) {
            return false;
        }
        if (isModExist("confluence")) {
            return false;
        }
        if (LEVEL_CHUNK_ARENA_MIXIN.equals(mixinClassName)) {
            return !config.enableServerRuntimeArenaPalette;
        }
        if (VOXY_ARENA_MIXIN.equals(mixinClassName)) {
            return config.enableClientArenaPalette;
        }
        return true;
    }

    private static ModFileInfo getModFile(String modId) {
        LoadingModList modList = LoadingModList.get();
        ModFileInfo modFile = modList.getModFileById(modId);
        if (modFile != null) {
            return modFile;
        }

        return modList.getPlugins().stream()
                .filter(ModFileInfo.class::isInstance)
                .map(ModFileInfo.class::cast)
                .filter(file -> file.getMods().stream().anyMatch(mod -> mod.getModId().equals(modId)))
                .findFirst()
                .orElse(null);
    }

    public static boolean isModExist(String modId) {
        return getModFile(modId) != null;
    }

    public static boolean hasClass(String className) {
        try {
            MixinService.getService().getBytecodeProvider().getClassNode(className);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
