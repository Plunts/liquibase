package liquibase.integration.ant;

import liquibase.Scope;
import liquibase.diff.DiffResult;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.StandardObjectChangeFilter;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.integration.ant.type.ChangeLogOutputFile;
import liquibase.parser.Unparser;
import liquibase.parser.UnparserFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.FileUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashSet;
import java.util.Set;

public class DiffDatabaseToChangeLogTask extends AbstractDatabaseDiffTask {
    private Set<ChangeLogOutputFile> changeLogOutputFiles = new LinkedHashSet<>();
    private boolean includeSchema = true;
    private boolean includeCatalog = true;
    private boolean includeTablespace = true;
    private String includeObjects;
    private String excludeObjects;

    @Override
    protected void executeWithLiquibaseClassloader() throws BuildException {
        for(ChangeLogOutputFile changeLogOutputFile : changeLogOutputFiles) {
            PrintStream printStream = null;
            String encoding = getOutputEncoding(changeLogOutputFile);
            try {
                FileResource outputFile = changeLogOutputFile.getOutputFile();
                Unparser unparser = changeLogOutputFile.getUnparser();
                printStream = new PrintStream(outputFile.getOutputStream(), true, encoding);
                DiffResult diffResult = getDiffResult();
                DiffOutputControl diffOutputControl = getDiffOutputControl();
                DiffToChangeLog diffToChangeLog = new DiffToChangeLog(diffResult, diffOutputControl);
                diffToChangeLog.print(printStream, unparser);
            } catch (UnsupportedEncodingException e) {
                throw new BuildException("Unable to diff databases to change log file. Encoding [" + encoding + "] is not supported.", e);
            } catch (IOException e) {
                throw new BuildException("Unable to diff databases to change log file. Error creating output stream.", e);
            } catch (ParserConfigurationException e) {
                throw new BuildException("Unable to diff databases to change log file. Error configuring parser.", e);
            } catch (DatabaseException e) {
                throw new BuildException("Unable to diff databases to change log file: " + e.getMessage(), e);
            } finally {
                FileUtils.close(printStream);
            }
        }
    }

    @Override
    protected void validateParameters() {
        super.validateParameters();

        if(changeLogOutputFiles.isEmpty()) {
            throw new BuildException("At least one output file element (<json>, <yaml>, <xml>, or <txt>)must be defined.");
        }
    }

    public String getOutputEncoding(ChangeLogOutputFile changeLogOutputFile) {
        String encoding = changeLogOutputFile.getEncoding();
        return (encoding == null) ? getDefaultOutputEncoding() : encoding;
    }

    private DiffOutputControl getDiffOutputControl() {
        DiffOutputControl diffOutputControl = new DiffOutputControl(includeCatalog, includeSchema, includeTablespace, null);

        if ((excludeObjects != null) && (includeObjects != null)) {
            throw new UnexpectedLiquibaseException("Cannot specify both excludeObjects and includeObjects");
        }
        if (excludeObjects != null) {
            diffOutputControl.setObjectChangeFilter(new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.EXCLUDE, excludeObjects));
        }
        if (includeObjects != null) {
            diffOutputControl.setObjectChangeFilter(new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.INCLUDE, includeObjects));
        }

        return diffOutputControl;
    }

    public void addConfiguredXml(ChangeLogOutputFile changeLogOutputFile) {
        changeLogOutputFile.setUnparser(Scope.getCurrentScope().getSingleton(UnparserFactory.class).getUnparser("file.xml"));
        changeLogOutputFiles.add(changeLogOutputFile);
    }

    public void addConfiguredYaml(ChangeLogOutputFile changeLogOutputFile) {
        changeLogOutputFile.setUnparser(Scope.getCurrentScope().getSingleton(UnparserFactory.class).getUnparser("file.yaml"));
        changeLogOutputFiles.add(changeLogOutputFile);
    }

    public void addConfiguredTxt(ChangeLogOutputFile changeLogOutputFile) {
        changeLogOutputFile.setUnparser(Scope.getCurrentScope().getSingleton(UnparserFactory.class).getUnparser("file.txt"));
        changeLogOutputFiles.add(changeLogOutputFile);
    }

    public boolean getIncludeCatalog() {
        return includeCatalog;
    }

    public void setIncludeCatalog(boolean includeCatalog) {
        this.includeCatalog = includeCatalog;
    }

    public boolean getIncludeSchema() {
        return includeSchema;
    }

    public void setIncludeSchema(boolean includeSchema) {
        this.includeSchema = includeSchema;
    }

    public boolean getIncludeTablespace() {
        return includeTablespace;
    }

    public void setIncludeTablespace(boolean includeTablespace) {
        this.includeTablespace = includeTablespace;
    }

    public String getIncludeObjects() {
        return includeObjects;
    }

    public void setIncludeObjects(String includeObjects) {
        this.includeObjects = includeObjects;
    }

    public String getExcludeObjects() {
        return excludeObjects;
    }

    public void setExcludeObjects(String excludeObjects) {
        this.excludeObjects = excludeObjects;
    }

}
