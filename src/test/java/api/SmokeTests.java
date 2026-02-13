package api;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
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

    /**
     * Decompress existing test archives
     * @param file the archive file name
     * @throws Exception if an error occurs during decompression
     */
    @ParameterizedTest
    @ValueSource(strings = {"jenkins-chart.tgz", "jenkins-sources.tar.gz"})
    @Disabled("Fixed on 1.29.0 https://issues.apache.org/jira/browse/COMPRESS-705")
    void uncompressArchives(String file) throws Exception {
        Path archive = Paths.get("src/test/resources").resolve(file);
        uncompressGzip(Files.newInputStream(archive));
    }

    @ParameterizedTest
    @ValueSource(strings = {"jenkins-chart.tgz", "jenkins-sources.tar.gz"})
    void compressZSTD(String file) throws Exception {
        Path archive = Paths.get("src/test/resources").resolve(file);
        compressZSTD(archive, targetDir.resolve("%s.xz".formatted(file)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"jenkins-chart.tgz", "jenkins-sources.tar.gz"})
    void compressXZ(String file) throws Exception {
        Path archive = Paths.get("src/test/resources").resolve(file);
        compressXZ(archive, targetDir.resolve("%s.xz".formatted(file)));
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

    private static void uncompressGzip(InputStream inputStream) throws IOException {
        Path tarFile = Files.createTempFile("jenkins", "temporary");
        try (BufferedInputStream bis = new BufferedInputStream(inputStream);
                GzipCompressorInputStream gzis = new GzipCompressorInputStream(bis);
                OutputStream fos = Files.newOutputStream(tarFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            gzis.transferTo(bos);
        }
    }
}
