package com.oakonell.findx.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.fraction.Fraction;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.oakonell.findx.FindXApp;
import com.oakonell.findx.R;
import com.oakonell.findx.custom.model.CustomLevelBuilder;
import com.oakonell.findx.custom.model.CustomLevelDBReader;
import com.oakonell.findx.custom.model.CustomLevelProxy;
import com.oakonell.findx.custom.model.CustomStage;
import com.oakonell.findx.data.DataBaseHelper;
import com.oakonell.findx.data.DataBaseHelper.CustomLevelTable;
import com.oakonell.findx.model.Level.LevelSolution;
import com.oakonell.findx.model.ops.Add;
import com.oakonell.findx.model.ops.Divide;
import com.oakonell.findx.model.ops.Multiply;
import com.oakonell.findx.model.ops.SquareRoot;
import com.oakonell.findx.model.ops.Subtract;
import com.oakonell.findx.model.ops.Swap;

public class Levels {
	private static Map<String, Stage> stages = new LinkedHashMap<String, Stage>();
	private static Map<String, Level> levels = new LinkedHashMap<String, Level>();
	private static CustomStage customStage;
	private static boolean reloadCustom = true;

	static {
		// Context context = FindXApp.getContext();

		Stage stage1 = new Stage("1", R.string.stage1_title,
				R.raw.prelude_no_8_in_e_flat_minor_loop, null);
		stages.put(stage1.getId(), stage1);

		configureStage1(stage1);

		Stage stage2 = new Stage("2", R.string.stage2_title,
				R.raw.partita_no_1_in_b_flat_major_praeludium, stage1);
		stages.put(stage2.getId(), stage2);
		configureStage2(stage2);

		Stage stage3 = new Stage("3", R.string.stage3_title,
				R.raw.partita_no_1_in_b_flat_major_pus_1_sarabande, stage2);
		stages.put(stage3.getId(), stage3);
		configureStage3(stage3);

		// new stages!
		Stage stage4 = new Stage("4", R.string.stage4_title,
				R.raw.partita_no_1_in_b_flat_major_pus_1_sarabande, stage3);
		stages.put(stage4.getId(), stage4);
		configureStage4(stage4);

		customStage = new CustomStage("C", R.string.custom_stage_title);

	}

	private static void configureStage1(Stage stage) {
		Expression left;
		Expression right;
		Equation eq;
		List<Operation> ops;

		// level
		left = new Expression(1, -1);
		right = new Expression(0);
		eq = new Equation(left, right); // x-1=0 , x = 1
		ops = new ArrayList<Operation>();
		ops.add(new Add(new Expression(1)));
		addLevel(stage, new Level(stage, "Trivial", eq, ops, Arrays.asList(0)));

		// level
		left = new Expression(1, 3);
		right = new Expression(7);
		eq = new Equation(left, right); // x + 3 = 7 , x = 4
		ops = new ArrayList<Operation>();
		ops.add(new Subtract(new Expression(1)));
		ops.add(new Add(new Expression(1)));
		addLevel(stage,
				new Level(stage, "Subtract", eq, ops, Arrays.asList(0, 0, 0)));

		// level
		left = new Expression(3, -18);
		right = new Expression(15);
		eq = new Equation(left, right); // 3x -18 = 15, x = 11
		ops = new ArrayList<Operation>();
		ops.add(new Divide(3));
		ops.add(new Add(new Expression(6)));
		ops.add(new Add(new Expression(18)));
		addLevel(
				stage,
				new Level(stage, "Add and Divide", eq, ops, Arrays.asList(0, 1)));

		// level
		left = new Expression(4, -132);
		right = new Expression(116);
		eq = new Equation(left, right); // 4x - 132 = 116, x = 62
		ops = new ArrayList<Operation>();
		ops.add(new Add(new Expression(33)));
		ops.add(new Divide(4));
		ops.add(new Add(new Expression(132)));
		addLevel(
				stage,
				new Level(stage, "Add and Divide 2", eq, ops, Arrays.asList(1,
						0)));

		// level
		left = new Expression(new Fraction(7, 2), new Fraction(-7, 3));
		right = new Expression(Fraction.ZERO, new Fraction(-7, 30));
		eq = new Equation(left, right); // 7/2x - 7/3 = -7/30
		ops = new ArrayList<Operation>();
		ops.add(new Add(new Expression(Fraction.ZERO, new Fraction(2, 3))));
		ops.add(new Divide(new Fraction(7, 2)));
		ops.add(new Add(new Expression(Fraction.ZERO, new Fraction(7, 3))));
		addLevel(stage,
				new Level(stage, "Fractions", eq, ops, Arrays.asList(1, 0)));

		// level
		left = new Expression(new Fraction(5, 4), new Fraction(3, 5));
		right = new Expression(Fraction.ZERO, new Fraction(69, 40));
		eq = new Equation(left, right); // 5/4x + 3/5 = 69/40, x= 9/10
		ops = new ArrayList<Operation>();
		ops.add(new Subtract(new Expression(Fraction.ZERO, new Fraction(3, 5))));
		ops.add(new Multiply(new Fraction(4, 5)));
		addLevel(
				stage,
				new Level(stage, "More Fractions", eq, ops, Arrays.asList(0, 1)));

		// level
		left = new Expression(new Fraction(5, 3), new Fraction(20));
		right = new Expression(Fraction.ZERO, new Fraction(65, 3));
		eq = new Equation(left, right); // 5/3x + 20 = 65/3
		ops = new ArrayList<Operation>();
		ops.add(new Add(new Expression(12)));
		ops.add(new Multiply(new Fraction(3, 5)));
		ops.add(new Divide(new Fraction(3, 5)));
		ops.add(new Subtract(new Expression(12)));
		addLevel(stage,
				new Level(stage, "Wrong Choices", eq, ops, Arrays.asList(1, 3)));

		// level
		left = new Expression(1, -4);
		right = new Expression(5);
		eq = new Equation(left, right); // x-4 = 5 , x =9
		ops = new ArrayList<Operation>();
		ops.add(new Subtract(new Expression(1)));
		ops.add(new Add(new Expression(3)));
		addLevel(
				stage,
				new Level(stage, "Add 'em up", eq, ops, Arrays.asList(0, 0, 1,
						1)));

		// level
		left = new Expression(1, 1);
		right = new Expression(9);
		eq = new Equation(left, right); // x + 1 = 9 , x =8
		ops = new ArrayList<Operation>();
		ops.add(new Add(new Expression(3)));
		ops.add(new Subtract(new Expression(5)));
		addLevel(
				stage,
				new Level(stage, "Add 'em up 2", eq, ops, Arrays.asList(0, 0,
						0, 1, 1)));

		// level
		left = new Expression(12, 0);
		right = new Expression(96);
		eq = new Equation(left, right); // 12x = 96 , x = 8
		ops = new ArrayList<Operation>();
		ops.add(new Divide(3));
		ops.add(new Divide(2));
		addLevel(
				stage,
				new Level(stage, "Divide and Conquer", eq, ops, Arrays.asList(
						0, 1, 1)));

		// level
		left = new Expression(4, 7);
		right = new Expression(15);
		eq = new Equation(left, right); // 4x + 7 =15 , x = x = 2
		ops = new ArrayList<Operation>();
		ops.add(new Subtract(new Expression(1)));
		ops.add(new Divide(2));
		addLevel(
				stage,
				new Level(stage, "Sub-division", eq, ops, Arrays.asList(0, 1,
						0, 1, 0)));

		// level
		left = new Expression(3, -2);
		right = new Expression(28);
		eq = new Equation(left, right); // 3x - 2 = 28 , x = 10
		ops = new ArrayList<Operation>();
		ops.add(new Add(new Expression(3)));
		ops.add(new Multiply(3));
		ops.add(new Divide(3));
		addLevel(
				stage,
				new Level(stage, "Multiply", eq, ops, Arrays.asList(1, 0, 0, 2,
						2)));
	}

	private static void configureStage2(Stage stage) {
		Expression left;
		Expression right;
		Equation eq;
		List<Operation> ops;

		// level
		left = new Expression(2, -1);
		right = new Expression(1, 0);
		eq = new Equation(left, right); // 2x-1 = x , x = 1
		ops = new ArrayList<Operation>();
		ops.add(new Add(new Expression(-1, 1)));
		ops.add(new Add(new Expression(-1, 0)));
		ops.add(new Add(new Expression(0, 1)));
		addLevel(stage,
				new Level(stage, "Not so Trivial", eq, ops, Arrays.asList(0)));

		// level
		left = new Expression(-3, 4);
		right = new Expression(5);
		eq = new Equation(left, right); // -3x + 4 = 5 , x = -3
		ops = new ArrayList<Operation>();
		ops.add(new Subtract(new Expression(1)));
		ops.add(new Multiply(-1));
		ops.add(new Divide(3));
		addLevel(
				stage,
				new Level(stage, "Don't be negative", eq, ops, Arrays.asList(0,
						2, 0, 1)));

		// level
		left = new Expression(6, 3);
		right = new Expression(2, 15);
		eq = new Equation(left, right); // 6x + 3 = 2x + 15 , x = 3
		ops = new ArrayList<Operation>();
		ops.add(new Subtract(new Expression(1)));
		ops.add(new Subtract(new Expression(1, 0)));
		ops.add(new Divide(2));
		addLevel(
				stage,
				new Level(stage, "Too many x's", eq, ops, Arrays.asList(0, 2,
						0, 1, 2)));

		// level
		left = new Expression(1, 0);
		right = new Expression(3, -6);
		eq = new Equation(left, right); // x = 3x-6 , x = 3
		ops = new ArrayList<Operation>();
		ops.add(new Add(new Expression(3)));
		ops.add(new Divide(2));
		ops.add(new Subtract(new Expression(1, 0)));
		ops.add(new Swap());
		addLevel(
				stage,
				new Level(stage, "Stay to the left", eq, ops, Arrays.asList(2,
						1, 0, 3)));

		// level
		left = new Expression(49, 32);
		right = new Expression(81);
		eq = new Equation(left, right); // 49x + 32 = 81 , x=1
		ops = new ArrayList<Operation>();
		ops.add(new Add(new Expression(3)));
		ops.add(new Subtract(new Expression(5)));
		ops.add(new Divide(new Fraction(7)));
		addLevel(
				stage,
				new Level(stage, "Getting Bigger", eq, ops, Arrays.asList(0, 2,
						1, 2)));

		// level
		left = new Expression(3, -90);
		right = new Expression(-87);
		eq = new Equation(left, right); // 49x + 32 = 81, x=1
		ops = new ArrayList<Operation>();
		ops.add(new Multiply(new Fraction(3)));
		ops.add(new Add(new Expression(Fraction.ZERO, new Fraction(6))));
		ops.add(new Divide(new Fraction(9)));
		addLevel(
				stage,
				new Level(stage, "A Different Way", eq, ops, Arrays.asList(2,
						1, 0, 1, 1)));

		// level
		left = new Expression(3, -1);
		right = new Expression(1, 0);
		eq = new Equation(left, right); // 3x-1 = x , x = 1
		ops = new ArrayList<Operation>();
		ops.add(new Add(new Expression(-1, 1)));
		ops.add(new Add(new Expression(-1, 0)));
		ops.add(new Add(new Expression(0, 1)));
		ops.add(new Divide(2));
		addLevel(stage,
				new Level(stage, "More than X", eq, ops, Arrays.asList(0, 3)));

		// level
		left = new Expression(8, -10);
		right = new Expression(6, -8);
		eq = new Equation(left, right); // 8x-10 = 6x-8
		ops = new ArrayList<Operation>();
		ops.add(new Add(new Expression(5)));
		ops.add(new Subtract(new Expression(3, 0)));
		ops.add(new Divide(2));
		addLevel(
				stage,
				new Level(stage, "One of each", eq, ops, Arrays.asList(2, 1, 0)));

		// level
		left = new Expression(1, -18);
		right = new Expression(-5);
		eq = new Equation(left, right); // x - 18 = -5
		ops = new ArrayList<Operation>();
		ops.add(new Add(new Expression(3)));
		ops.add(new Subtract(new Expression(7)));
		ops.add(new Add(new Expression(11)));
		addLevel(stage,
				new Level(stage, "Primes", eq, ops, Arrays.asList(2, 2, 1, 0)));

		// level
		left = new Expression(new Fraction(1, 8), new Fraction(-5));
		right = new Expression(Fraction.ZERO, new Fraction(-19, 4));
		eq = new Equation(left, right); // 1/8x - 5 = -19/4
		ops = new ArrayList<Operation>();
		ops.add(new Add(new Expression(5)));
		ops.add(new Multiply(2));
		addLevel(
				stage,
				new Level(stage, "Get Even", eq, ops, Arrays.asList(0, 1, 1, 1)));

		// level
		left = new Expression(new Fraction(-1), new Fraction(1, 2));
		right = new Expression(new Fraction(-3, 2), new Fraction(4));
		eq = new Equation(left, right); // -x + 1/2 = -3/2x + 4
		ops = new ArrayList<Operation>();
		ops.add(new Add(new Expression(3, 0)));
		ops.add(new Subtract(new Expression(1)));
		ops.add(new Multiply(2));
		addLevel(stage,
				new Level(stage, "That's Odd", eq, ops, Arrays.asList(2, 0, 1)));

		// level
		left = new Expression(-6, -9);
		right = new Expression(-9, -9);
		eq = new Equation(left, right); // -6x - 9 = -9x - 9
		ops = new ArrayList<Operation>();
		ops.add(new Add(new Expression(0, 3)));
		ops.add(new Add(new Expression(3, 0)));
		ops.add(new Divide(3));
		addLevel(
				stage,
				new Level(stage, "Power of Three", eq, ops, Arrays.asList(2, 1,
						0)));

	}

	private static void configureStage3(Stage stage) {
		Expression left;
		Expression right;
		Equation eq;
		List<Operation> ops;

		// level
		left = new Expression(7, -14);
		right = new Expression(6, -9);
		eq = new Equation(left, right); // 7x - 14 = 6x - 9 , x=5
		ops = new ArrayList<Operation>();
		ops.add(new Add(new Expression(Fraction.ZERO, new Fraction(7))));
		ops.add(new Subtract(new Expression(3, 0)));
		ops.add(new Multiply(new Fraction(2)));
		ops.add(new Divide(new Fraction(2)));
		addLevel(
				stage,
				new Level(stage, "Medium 3", eq, ops, Arrays.asList(3, 0, 1, 2)));

		// level
		left = new Expression(-1, -2);
		right = new Expression(2, -8);
		eq = new Equation(left, right); // -x - 2 = 2x -8 , x = 2
		ops = new ArrayList<Operation>();
		ops.add(new Add(new Expression(3)));
		ops.add(new Divide(3));
		ops.add(new Multiply(3));
		ops.add(new Add(new Expression(9, 0)));
		ops.add(new Swap());
		addLevel(
				stage,
				new Level(stage, "Build it up", eq, ops, Arrays.asList(0, 0, 2,
						0, 0, 2, 3, 1, 1, 1, 4)));

		// level
		left = new Expression(-3, 11);
		right = new Expression(-6, 2);
		eq = new Equation(left, right); // -3x + 11 = -6x + 2, x=-3
		ops = new ArrayList<Operation>();
		ops.add(new Add(new Expression(3)));
		ops.add(new Divide(3));
		ops.add(new Multiply(3));
		ops.add(new Multiply(-1));
		ops.add(new Subtract(new Expression(1, 0)));
		addLevel(
				stage,
				new Level(stage, "Multi-Sub", eq, ops, Arrays.asList(2, 0, 1,
						3, 0, 1, 0, 4, 4, 3)));

		// level
		left = new Expression(400);
		right = new Expression(3, 100);
		eq = new Equation(left, right); // 400 = 3x + 100
		ops = new ArrayList<Operation>();
		ops.add(new Add(new Expression(50)));
		ops.add(new Divide(3));
		ops.add(new Subtract(new Expression(50)));
		ops.add(new Swap());
		addLevel(
				stage,
				new Level(stage, "Per Cent", eq, ops, Arrays.asList(2, 2, 1, 3)));

		// level
		left = new Expression(-4, 1);
		right = new Expression(new Fraction(-9, 2), new Fraction(4));
		eq = new Equation(left, right); // -4x + 1 = -9/2x + 4
		ops = new ArrayList<Operation>();
		ops.add(new Swap());
		ops.add(new Add(new Expression(3, 0)));
		ops.add(new Multiply(-2));
		ops.add(new Subtract(new Expression(5)));
		ops.add(new Add(new Expression(1, 1)));
		addLevel(
				stage,
				new Level(stage, "One of Each Again", eq, ops, Arrays.asList(0,
						1, 3, 4, 2)));

		// level
		left = new Expression(16, 30);
		right = new Expression(190);
		eq = new Equation(left, right); // 16x + 30 = 190
		ops = new ArrayList<Operation>();
		ops.add(new Add(new Expression(10)));
		ops.add(new Subtract(new Expression(10)));
		ops.add(new Divide(2));
		addLevel(
				stage,
				new Level(stage, "Scaling down", eq, ops, Arrays.asList(0, 2,
						2, 1, 2, 2)));

		// level
		left = new Expression(91);
		right = new Expression(8, -5);
		eq = new Equation(left, right); // 91 = 8x - 5
		ops = new ArrayList<Operation>();
		ops.add(new Add(new Expression(1)));
		ops.add(new Multiply(2));
		ops.add(new Swap());
		ops.add(new Divide(4));
		ops.add(new Subtract(new Expression(2)));
		addLevel(
				stage,
				new Level(stage, "Half a Dozen", eq, ops, Arrays.asList(0, 2,
						3, 0, 1, 3)));

		// level
		left = new Expression(-36, -39);
		right = new Expression(-9, -66);
		eq = new Equation(left, right); // -36x - 39 = -9x - 66
		ops = new ArrayList<Operation>();
		ops.add(new Subtract(new Expression(-3, -13)));
		ops.add(new Divide(-3));
		addLevel(
				stage,
				new Level(stage, "The Great Divide", eq, ops, Arrays.asList(0,
						0, 0, 1, 1, 1)));

		// level
		left = new Expression(3, 1536);
		right = new Expression(7, 1024);
		eq = new Equation(left, right); // 3x + 1536 = 7x + 1024, x = 512/3
		ops = new ArrayList<Operation>();
		ops.add(new Subtract(new Expression(1)));
		ops.add(new Divide(2));
		ops.add(new Multiply(2));
		ops.add(new Subtract(new Expression(1, 0)));
		ops.add(new Swap());
		addLevel(
				stage,
				new Level(stage, "Fract-ured", eq, ops, Arrays
						.asList(3, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2,
								2, 2, 2, 2, 2, 4)));
	}

	private static void configureStage4(Stage stage) {
		Expression left;
		Expression right;
		Equation eq;
		List<Operation> ops;
		List<Fraction> solutions = new ArrayList<Fraction>();
		LevelSolution levelSolution;

		// level
		left = new Expression(1, 0, 0);
		right = new Expression(0);
		eq = new Equation(left, right); // x^2 = 0
		ops = new ArrayList<Operation>();
		ops.add(new SquareRoot());
		solutions.add(Fraction.ZERO);
		levelSolution = new LevelSolution(Arrays.asList(0), eq, ops);

		addLevel(stage, new Level(stage, "Trivial Square", eq, ops,
				levelSolution));

		// level
		solutions = new ArrayList<Fraction>();
		left = new Expression(1, 0, -1);
		right = new Expression(0);
		eq = new Equation(left, right); // x^2 - 1 = 0
		ops = new ArrayList<Operation>();
		ops.add(new Add(new Expression(1)));
		ops.add(new SquareRoot());
		solutions.add(Fraction.ONE);
		solutions.add(Fraction.MINUS_ONE);
		levelSolution = new LevelSolution(solutions, Arrays.asList(0, 1),
				new Equation(new Expression(1, 0), new Expression(1)),
				Collections.<Integer> emptyList(), new Equation(new Expression(
						1, 0), new Expression(-1)),
				Collections.<Integer> emptyList());

		addLevel(stage, new Level(stage, "Hip to be Square", eq, ops,
				levelSolution));
	}

	private static void addLevel(Stage stage, Level level) {
		stage.addLevel(level);
		levels.put(level.getId(), level);
	}

	public static ILevel get(String key) {
		if (isCustom(key)) {
			return customStage.getLevel(key);
		}
		return levels.get(key);
	}

	private static boolean isCustom(String key) {
		return key.startsWith("C");
	}

	public static List<Stage> getStages() {
		return new ArrayList<Stage>(stages.values());
	}

	public static Stage getStage(String stageId) {
		return stages.get(stageId);
	}

	public static synchronized CustomStage getCustomStage() {
		if (reloadCustom) {
			configureCustom(customStage);
		}
		return customStage;

	}

	public static synchronized void resetCustomStage() {
		configureCustom(customStage);
	}

	private static void configureCustom(CustomStage custom) {
		// TODO not thread safe access to custom stage..?!
		custom.getLevels().clear();

		DataBaseHelper helper = new DataBaseHelper(FindXApp.getContext());
		SQLiteDatabase db = helper.getReadableDatabase();

		// read from the DB
		Cursor query = db.query(DataBaseHelper.CUSTOM_LEVEL_TABLE_NAME, null,
				CustomLevelTable.TO_DELETE + " is null or "
						+ CustomLevelTable.TO_DELETE + " = 0"
				//
				, null, null, null, CustomLevelTable.SEQ_NUM);
		CustomLevelDBReader reader = new CustomLevelDBReader();
		int num = 1;
		while (query.moveToNext()) {
			long dbId = query.getLong(query.getColumnIndex(BaseColumns._ID));
			String name = query.getString(query
					.getColumnIndex(DataBaseHelper.CustomLevelTable.NAME));

			boolean isImported = query.getInt(query
					.getColumnIndex(DataBaseHelper.CustomLevelTable.IS_IMPORTED)) > 0;
			String author = query.getString(query
					.getColumnIndex(DataBaseHelper.CustomLevelTable.AUTHOR));
			String serverId = query.getString(query
					.getColumnIndex(DataBaseHelper.CustomLevelTable.SERVER_ID));
			int sequence = query.getInt(query
					.getColumnIndex(DataBaseHelper.CustomLevelTable.SEQ_NUM));

			
			// TODO make this only read the custom level main info
			// and lazily load the other info on demand
			CustomLevelProxy level = new CustomLevelProxy(custom, "C-" + num,
					dbId, name, isImported, author, serverId, sequence);
			custom.addLevel(level);
			num++;
		}
		query.close();
		db.close();
		reloadCustom = false;
	}

}
