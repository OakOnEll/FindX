package com.oakonell.findx.custom;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oakonell.findx.R;
import com.oakonell.findx.custom.model.AbstractEquationSolver.OnCalculateMove;
import com.oakonell.findx.custom.model.AbstractEquationSolver.SolverState;
import com.oakonell.findx.custom.model.CustomLevelBuilder;
import com.oakonell.findx.custom.model.EquationSolver;
import com.oakonell.findx.model.Move;


class CalculateMinMovesProgressTask extends AsyncTask<Void, SolverState, List<Move>> {
    // launch a progress dialog
    // updated as the model calculates each level up to the user's
    // num input moves
    // it is cancelable
    // when finding a set of moves, present user the moves, and give
    // option to update

    // Handle keeping the async task running while simple orientation change
    // occurs
    // based on
    // http://stackoverflow.com/questions/3821423/background-task-progress-dialog-orientation-change-is-there-any-100-working/3821998#3821998
    private CustomPuzzleBuilderActivity parent;

    static class DialogInfo {
        private Dialog dialog;
        private TextView progressText;
        private TextView maxText;
        private ProgressBar progress;
        private TextView solutionFound;
    }
    DialogInfo dialogInfo;
    private CustomLevelBuilder builder;
    private long lastNumUpdated = 0;
    private int lastSolutionDepth = 1000;
    private SolverState solverState;
    private NumberFormat format = new DecimalFormat();

    public CalculateMinMovesProgressTask(CustomPuzzleBuilderActivity context, CustomLevelBuilder builder) {
        parent = context;
        this.builder = builder;
        format.setGroupingUsed(true);
    }

    @Override
    protected void onPreExecute() {
        showDialog();
        dialogInfo.progress.setProgress(0);
        dialogInfo.progressText.setText("0");
    }

    private void showDialog() {
        dialogInfo = new DialogInfo();
        // Builder dialog = new AlertDialog.Builder(parent);
        dialogInfo.dialog = new Dialog(parent);
        dialogInfo.dialog.setContentView(R.layout.search_solution_progress);
        dialogInfo.dialog.setTitle(R.string.searching);
        // dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // dialog.setMax(100);
        // dialog.setProgress(0);
        // dialog.setIndeterminate(false);
        dialogInfo.dialog.setCancelable(true);
        Button cancelButton = (Button) dialogInfo.dialog.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO confirm cancel?
                cancel(true);
            }
        });

        dialogInfo.progressText = (TextView) dialogInfo.dialog.findViewById(R.id.value);
        dialogInfo.maxText = (TextView) dialogInfo.dialog.findViewById(R.id.max);
        dialogInfo.progress = (ProgressBar) dialogInfo.dialog.findViewById(R.id.progress);
        dialogInfo.solutionFound = (TextView) dialogInfo.dialog.findViewById(R.id.found_solution);
        dialogInfo.dialog.show();
    }

    @Override
    protected List<Move> doInBackground(Void... params) {
        EquationSolver solver = new EquationSolver();
        OnCalculateMove onCalculateMove = new OnCalculateMove() {
            @Override
            public boolean shouldContinue() {
                return !isCancelled();
            }

            @Override
            public void calculated(SolverState currentState) {
                solverState = currentState;
                if (lastNumUpdated == 0 || currentState.getNumMovesVisited() - lastNumUpdated > 100) {
                    lastNumUpdated = currentState.getNumMovesVisited();
                    publishProgress(currentState);
                    // try {
                    // Thread.sleep(10);
                    // } catch (InterruptedException e) {
                    // // TODO Auto-generated catch block
                    // e.printStackTrace();
                    // }
                }
            }
        };
        List<Move> solution = solver.solve(builder.getMoves().get(0).getStartEquation(), builder.getOperations(),
                builder.getMoves().size() - 2, onCalculateMove);
        return solution;
    }

    @Override
    protected void onCancelled() {
        dialogInfo.dialog.cancel();
        if (solverState != null && solverState.hasCurrentSolution()) {
            promptToReplaceMoves(solverState.getCurrentSolution(), false);
        }
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(final List<Move> result) {
        if (result == null || result.size() >= builder.getMoves().size() - 1) {
            builder.markAsOptimized();
            // TODO notify parent activity to update the optimized display?
            final AlertDialog alertDialog = new AlertDialog.Builder(parent).create();
            alertDialog.setTitle(R.string.already_minimum_moves_title);
            alertDialog
                    .setMessage(parent.getResources().getString(R.string.already_minimum_moves,
                            builder.getMoves().size() - 1));
            alertDialog.setButton(parent.getText(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int which) {
                    alertDialog.dismiss();
                    dialogInfo.dialog.dismiss();
                    parent.task = null;
                }
            });
            alertDialog.show();
        } else {
            promptToReplaceMoves(result, true);
        }
    }

    private void promptToReplaceMoves(final List<Move> result, final boolean optimal) {
        final AlertDialog alertDialog = new AlertDialog.Builder(parent).create();
        alertDialog.setTitle(R.string.better_solution_title);
        alertDialog
                .setMessage(parent.getResources().getString(R.string.better_solution, result.size()));
        alertDialog.setButton(parent.getText(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                builder.replaceMoves(result);
                if (optimal) {
                    builder.markAsOptimized();
                }
                parent.adapter.notifyDataSetChanged();
                alertDialog.dismiss();
                dialogInfo.dialog.dismiss();
                parent.task = null;
            }
        });
        alertDialog.setButton2(parent.getText(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                alertDialog.dismiss();
                dialogInfo.dialog.dismiss();
                parent.task = null;
            }
        });
        alertDialog.show();
    }

    @Override
    protected void onProgressUpdate(SolverState... values) {
        if (dialogInfo == null) {
            return;
        }
        SolverState solverState = values[0];
        long numMovesVisited = solverState.getNumMovesVisited();
        dialogInfo.progressText.setText(format.format(numMovesVisited));
        long totalMoveSpace = solverState.getTotalMoveSpace();
        dialogInfo.maxText.setText(format.format(totalMoveSpace));
        dialogInfo.progress.setMax(1000);
        double percentTimesTen = (1000.0 * numMovesVisited) / totalMoveSpace;
        dialogInfo.progress.setProgress((int) percentTimesTen);

        int currentSolutionDepth = solverState.getCurrentSolutionDepth();
        // TODO
        if (currentSolutionDepth > 0 && currentSolutionDepth < lastSolutionDepth) {
            lastSolutionDepth = currentSolutionDepth;
            dialogInfo.solutionFound.setVisibility(View.VISIBLE);
            dialogInfo.solutionFound.setText(parent.getResources().getString(R.string.found_solution,
                    currentSolutionDepth));
        }
    }

    public void setParent(CustomPuzzleBuilderActivity newParent) {
        if (dialogInfo != null && dialogInfo.dialog != null) {
            dialogInfo.dialog.dismiss();
            dialogInfo = null;
        }
        parent = newParent;
        if (parent != null) {
            showDialog();
        }
    }

}
