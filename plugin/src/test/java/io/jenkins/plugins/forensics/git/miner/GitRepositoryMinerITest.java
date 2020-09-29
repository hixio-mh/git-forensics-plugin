package io.jenkins.plugins.forensics.git.miner;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import edu.hm.hafner.util.FilteredLog;

import io.jenkins.plugins.forensics.git.util.GitITest;
import io.jenkins.plugins.forensics.miner.FileStatistics;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

/**
 * Tests the class {@link GitRepositoryMiner}.
 *
 * @author Ullrich Hafner
 */
public class GitRepositoryMinerITest extends GitITest {
    private static final String OTHER_FILE = "file";
    private static final FilteredLog LOG = new FilteredLog(StringUtils.EMPTY);

    /**
     * Verifies that the single file of the default initialization does have 1 commit and author.
     *
     * @throws InterruptedException
     *         never thrown
     */
    @Test
    public void shouldCollectSingleFile() throws InterruptedException {
        RepositoryStatistics statisticsPerFile = createRepositoryStatistics();

        assertThat(statisticsPerFile).hasFiles(OTHER_FILE);

        assertDefaultFileStatistics(statisticsPerFile);
    }

    /**
     * Verifies that the number of commits is correctly detected.
     *
     * @throws InterruptedException
     *         never thrown
     */
    @Test
    public void shouldCountNumberOfCommits() throws InterruptedException {
        writeFileAsAuthorFoo("First\n");
        String firstCommit = getHead();
        writeFileAsAuthorFoo("Second\nLine\n");
        String head = getHead();

        RepositoryStatistics statisticsPerFile = createRepositoryStatistics();

        assertThat(statisticsPerFile).hasFiles(OTHER_FILE, ADDITIONAL_FILE);

        assertDefaultFileStatistics(statisticsPerFile);

        FileStatistics fileStatistics = statisticsPerFile.get(ADDITIONAL_FILE);
        assertThat(fileStatistics).hasFileName(ADDITIONAL_FILE)
                .hasCommits(firstCommit, head)
                .hasNumberOfAuthors(1)
                .hasNumberOfCommits(2)
                .hasLinesOfCode(2);

        assertThat(fileStatistics.getAddedLines(firstCommit)).isOne();
        assertThat(fileStatistics.getDeletedLines(firstCommit)).isZero();
        assertThat(fileStatistics.getAddedLines(head)).isEqualTo(2);
        assertThat(fileStatistics.getDeletedLines(head)).isOne();
    }

    /**
     * Verifies that the number of distinct authors is correctly detected.
     *
     * @throws InterruptedException
     *         never thrown
     */
    @Test
    public void shouldCountNumberOfAuthors() throws InterruptedException {
        writeFileAsAuthorFoo("First");
        writeFileAsAuthorBar("Second");
        writeFileAsAuthorFoo("First");
        writeFileAsAuthorBar("Second");

        RepositoryStatistics statisticsPerFile = createRepositoryStatistics();

        assertThat(statisticsPerFile).hasFiles(OTHER_FILE, ADDITIONAL_FILE);

        assertDefaultFileStatistics(statisticsPerFile);

        FileStatistics fileStatistics = statisticsPerFile.get(ADDITIONAL_FILE);
        assertThat(fileStatistics).hasNumberOfAuthors(2);
        assertThat(fileStatistics).hasNumberOfCommits(4);
    }

    private RepositoryStatistics createRepositoryStatistics() throws InterruptedException {
        return new GitRepositoryMiner(createGitClient()).mine(new RepositoryStatistics(), LOG);
    }

    private void assertDefaultFileStatistics(final RepositoryStatistics statistics) {
        FileStatistics fileStatistics = statistics.get(OTHER_FILE);
        assertThat(fileStatistics).hasFileName(OTHER_FILE)
                .hasNumberOfAuthors(1)
                .hasNumberOfCommits(1)
                .hasLinesOfCode(0)
                .hasChurn(0);
    }
}
