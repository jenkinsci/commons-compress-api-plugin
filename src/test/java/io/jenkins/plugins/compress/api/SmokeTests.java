package io.jenkins.plugins.compress.api;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Smoke tests for decompression and compression of archives using several formats
 */
@Execution(ExecutionMode.CONCURRENT)
class SmokeTests {

    @TempDir(cleanup = CleanupMode.ON_SUCCESS)
    private static Path targetDir;

    @ParameterizedTest
    @ValueSource(strings = {"jenkins-chart.tgz", "jenkins-sources.tar.gz"})
    @Disabled("Fixed on commons-compress 1.29.0 https://issues.apache.org/jira/browse/COMPRESS-705")
    void uncompressGzip(String file) throws Exception {
        Path archive = Paths.get("src/test/resources").resolve(file);
        uncompressGzip(Files.newInputStream(archive));
    }

    @ParameterizedTest
    @ValueSource(strings = {"jenkins-chart.tar.zst", "jenkins-sources.tar.zst"})
    void uncompressZSTD(String file) throws Exception {
        Path archive = Paths.get("src/test/resources").resolve(file);
        uncompressZSTD(Files.newInputStream(archive));
    }

    @ParameterizedTest
    @ValueSource(strings = {"jenkins-chart.tar.xz", "jenkins-sources.tar.xz"})
    void uncompressXZ(String file) throws Exception {
        Path archive = Paths.get("src/test/resources").resolve(file);
        uncompressXZ(Files.newInputStream(archive));
    }

    @ParameterizedTest
    @ValueSource(strings = {"jenkins-chart.tar", "jenkins-sources.tar"})
    void compressGzip(String file) throws Exception {
        Path archive = Paths.get("src/test/resources").resolve(file);
        compressGzip(archive, targetDir.resolve("%s.gz".formatted(file)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"jenkins-chart.tar", "jenkins-sources.tar"})
    void compressZSTD(String file) throws Exception {
        Path archive = Paths.get("src/test/resources").resolve(file);
        compressZSTD(archive, targetDir.resolve("%s.xz".formatted(file)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"jenkins-chart.tar", "jenkins-sources.tar"})
    void compressXZ(String file) throws Exception {
        Path archive = Paths.get("src/test/resources").resolve(file);
        compressXZ(archive, targetDir.resolve("%s.xz".formatted(file)));
    }

    private void compressGzip(Path file, Path target) throws IOException {
        try (InputStream fis = Files.newInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                OutputStream fos = Files.newOutputStream(target);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos)) {
            bis.transferTo(gzos);
        }
    }

    private void compressZSTD(Path file, Path target) throws IOException {
        try (InputStream fis = Files.newInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                OutputStream fos = Files.newOutputStream(target);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                ZstdCompressorOutputStream zstdos = new ZstdCompressorOutputStream(bos)) {
            bis.transferTo(zstdos);
        }
    }

    private void compressXZ(Path file, Path target) throws IOException {
        try (InputStream fis = Files.newInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                OutputStream fos = Files.newOutputStream(target);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                XZCompressorOutputStream zstdos = new XZCompressorOutputStream(bos)) {
            bis.transferTo(zstdos);
        }
    }

    private void uncompressGzip(InputStream inputStream) throws IOException {
        Path tarFile = Files.createTempFile("jenkins", "temporary");
        try (BufferedInputStream bis = new BufferedInputStream(inputStream);
                GzipCompressorInputStream gzis = new GzipCompressorInputStream(bis);
                OutputStream fos = Files.newOutputStream(tarFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            gzis.transferTo(bos);
        }
    }

    private void uncompressZSTD(InputStream inputStream) throws IOException {
        Path tarFile = Files.createTempFile("jenkins", "temporary");
        try (BufferedInputStream bis = new BufferedInputStream(inputStream);
                ZstdCompressorOutputStream zstdos = new ZstdCompressorOutputStream(Files.newOutputStream(tarFile))) {
            bis.transferTo(zstdos);
        }
    }

    private void uncompressXZ(InputStream inputStream) throws IOException {
        Path tarFile = Files.createTempFile("jenkins", "temporary");
        try (BufferedInputStream bis = new BufferedInputStream(inputStream);
                XZCompressorOutputStream zstdos = new XZCompressorOutputStream(Files.newOutputStream(tarFile))) {
            bis.transferTo(zstdos);
        }
    }
}
