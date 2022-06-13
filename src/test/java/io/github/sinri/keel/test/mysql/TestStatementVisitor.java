package io.github.sinri.keel.test.mysql;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterSession;
import net.sf.jsqlparser.statement.alter.AlterSystemStatement;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.synonym.CreateSynonym;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.statement.values.ValuesStatement;

public class TestStatementVisitor implements StatementVisitor {
    private final KeelLogger logger;

    public TestStatementVisitor() {
        logger = Keel.outputLogger(TestStatementVisitor.class.getSimpleName());
    }

    protected KeelLogger getLogger() {
        return logger;
    }

    private void printVisitObject(Object object) {
        getLogger().info(object.getClass().getName() + " : " + object);
    }

    @Override
    public void visit(SavepointStatement savepointStatement) {
        printVisitObject(savepointStatement);

    }

    @Override
    public void visit(RollbackStatement rollbackStatement) {
        printVisitObject(rollbackStatement);

    }

    @Override
    public void visit(Comment comment) {
        printVisitObject(comment);

    }

    @Override
    public void visit(Commit commit) {
        printVisitObject(commit);

    }

    @Override
    public void visit(Delete delete) {
        printVisitObject(delete);

    }

    @Override
    public void visit(Update update) {
        printVisitObject(update);

    }

    @Override
    public void visit(Insert insert) {
        printVisitObject(insert);

    }

    @Override
    public void visit(Replace replace) {
        printVisitObject(replace);

    }

    @Override
    public void visit(Drop drop) {
        printVisitObject(drop);

    }

    @Override
    public void visit(Truncate truncate) {
        printVisitObject(truncate);

    }

    @Override
    public void visit(CreateIndex createIndex) {
        printVisitObject(createIndex);

    }

    @Override
    public void visit(CreateSchema createSchema) {
        printVisitObject(createSchema);

    }

    @Override
    public void visit(CreateTable createTable) {
        printVisitObject(createTable);

    }

    @Override
    public void visit(CreateView createView) {
        printVisitObject(createView);

    }

    @Override
    public void visit(AlterView alterView) {
        printVisitObject(alterView);

    }

    @Override
    public void visit(Alter alter) {
        printVisitObject(alter);

    }

    @Override
    public void visit(Statements statements) {
        printVisitObject(statements);
        statements.getStatements().forEach(statement -> {

        });
    }

    @Override
    public void visit(Execute execute) {
        printVisitObject(execute);

    }

    @Override
    public void visit(SetStatement setStatement) {
        printVisitObject(setStatement);

    }

    @Override
    public void visit(ResetStatement resetStatement) {
        printVisitObject(resetStatement);

    }

    @Override
    public void visit(ShowColumnsStatement showColumnsStatement) {
        printVisitObject(showColumnsStatement);

    }

    @Override
    public void visit(ShowTablesStatement showTablesStatement) {
        printVisitObject(showTablesStatement);

    }

    @Override
    public void visit(Merge merge) {
        printVisitObject(merge);

    }

    @Override
    public void visit(Select select) {
        printVisitObject(select);
        select.getSelectBody().accept(new SelectVisitor() {
            @Override
            public void visit(PlainSelect plainSelect) {
                printVisitObject(plainSelect);
            }

            @Override
            public void visit(SetOperationList setOperationList) {
                printVisitObject(setOperationList);
            }

            @Override
            public void visit(WithItem withItem) {
                printVisitObject(withItem);
            }

            @Override
            public void visit(ValuesStatement valuesStatement) {
                printVisitObject(valuesStatement);
            }
        });
    }

    @Override
    public void visit(Upsert upsert) {
        printVisitObject(upsert);

    }

    @Override
    public void visit(UseStatement useStatement) {
        printVisitObject(useStatement);

    }

    @Override
    public void visit(Block block) {
        printVisitObject(block);

    }

    @Override
    public void visit(ValuesStatement valuesStatement) {
        printVisitObject(valuesStatement);

    }

    @Override
    public void visit(DescribeStatement describeStatement) {
        printVisitObject(describeStatement);

    }

    @Override
    public void visit(ExplainStatement explainStatement) {
        printVisitObject(explainStatement);

    }

    @Override
    public void visit(ShowStatement showStatement) {
        printVisitObject(showStatement);

    }

    @Override
    public void visit(DeclareStatement declareStatement) {
        printVisitObject(declareStatement);

    }

    @Override
    public void visit(Grant grant) {
        printVisitObject(grant);

    }

    @Override
    public void visit(CreateSequence createSequence) {
        printVisitObject(createSequence);

    }

    @Override
    public void visit(AlterSequence alterSequence) {
        printVisitObject(alterSequence);

    }

    @Override
    public void visit(CreateFunctionalStatement createFunctionalStatement) {
        printVisitObject(createFunctionalStatement);

    }

    @Override
    public void visit(CreateSynonym createSynonym) {
        printVisitObject(createSynonym);

    }

    @Override
    public void visit(AlterSession alterSession) {
        printVisitObject(alterSession);

    }

    @Override
    public void visit(IfElseStatement ifElseStatement) {
        printVisitObject(ifElseStatement);

    }

    @Override
    public void visit(RenameTableStatement renameTableStatement) {
        printVisitObject(renameTableStatement);

    }

    @Override
    public void visit(PurgeStatement purgeStatement) {
        printVisitObject(purgeStatement);

    }

    @Override
    public void visit(AlterSystemStatement alterSystemStatement) {
        printVisitObject(alterSystemStatement);

    }
}
