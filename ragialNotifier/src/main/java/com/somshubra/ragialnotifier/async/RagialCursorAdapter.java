package com.somshubra.ragialnotifier.async;

import android.content.Context;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.somshubra.ragialnotifier.R;
import com.somshubra.ragialnotifier.database.RagialDatabase;

public class RagialCursorAdapter extends SimpleCursorAdapter {
	private static int layout = R.layout.card_layout;

	private static String from[] = new String[] {RagialDatabase.COL_RAGIAL_NAME,
		RagialDatabase.COL_SHORT_COUNT, RagialDatabase.COL_SHORT_MIN, RagialDatabase.COL_SHORT_MAX, RagialDatabase.COL_SHORT_AVG, RagialDatabase.COL_SHORT_STD, RagialDatabase.COL_SHORT_CONFIDENCE,
		RagialDatabase.COL_LONG_COUNT, RagialDatabase.COL_LONG_MIN, RagialDatabase.COL_LONG_MAX, RagialDatabase.COL_LONG_AVG, RagialDatabase.COL_LONG_STD, RagialDatabase.COL_LONG_CONFIDENCE
	};

	private static int to[] = new int[] { R.id.ragial_name, 
		R.id.short_count, R.id.short_min, R.id.short_max, R.id.short_avg, R.id.short_std, R.id.short_conf,
		R.id.long_count, R.id.long_min, R.id.long_max, R.id.long_avg, R.id.long_std, R.id.long_conf };

	public RagialCursorAdapter(Context context) {
		super(context, layout, null, from, to, 0);
	}

	@Override
	public void setViewText(TextView v, String text) {
		super.setViewText(v, convertText(v, text));
	}

	private String convertText(TextView v, String text) {
		Double d = null;
		try {
			d = Double.parseDouble(text);
			d = Math.ceil(d);
		}
		catch (NumberFormatException e) {
			return text;
		}
		if(v.getId() == R.id.short_min) {
			if(d >= 1000000) {
				d /= 1000000;
				return d + "M";
			}
			else if(d >= 1000) {
				d /= 1000;
				return d + "K";
			}
			return text;
		}
		else if(v.getId() == R.id.short_max) {
			if(d >= 1000000) {
				d /= 1000000;
				return d + "M";
			}
			else if(d >= 1000) {
				d /= 1000;
				return d + "K";
			}
			return text;
		}
		else if(v.getId() == R.id.short_avg) {
			if(d >= 1000000) {
				d /= 1000000;
				return d + "M";
			}
			else if(d >= 1000) {
				d /= 1000;
				return d + "K";
			}
			return text;
		}
		else if(v.getId() == R.id.short_count) {
			return text;
		}
		else if(v.getId() == R.id.short_std) {
			if(d >= 1000000) {
				d /= 1000000;
				return d + "M";
			}
			else if(d >= 1000) {
				d /= 1000;
				return d + "K";
			}
			return text;
		}
		else if(v.getId() == R.id.short_conf) {
			return text;
		}
		else if(v.getId() == R.id.long_min) {
			if(d >= 1000000) {
				d /= 1000000;
				return d + "M";
			}
			else if(d >= 1000) {
				d /= 1000;
				return d + "K";
			}
			return text;
		}
		else if(v.getId() == R.id.long_max) {
			if(d >= 1000000) {
				d /= 1000000;
				return d + "M";
			}
			else if(d >= 1000) {
				d /= 1000;
				return d + "K";
			}
			return text;
		}
		else if(v.getId() == R.id.long_avg) {
			if(d >= 1000000) {
				d /= 1000000;
				return d + "M";
			}
			else if(d >= 1000) {
				d /= 1000;
				return d + "K";
			}
			return text;
		}
		else if(v.getId() == R.id.long_std) {
			if(d >= 1000000) {
				d /= 1000000;
				return d + "M";
			}
			else if(d >= 1000) {
				d /= 1000;
				return d + "K";
			}
			return text;
		}
		else if(v.getId() == R.id.long_count) {
			return text;
		}
		else if(v.getId() == R.id.long_conf) {
			return text;
		}

		return text;
	}


}
