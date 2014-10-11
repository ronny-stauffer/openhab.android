package org.openhab.habdroid.ui.topview;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.widget.ImageView;

import com.caverock.androidsvg.SVG;

import org.openhab.habdroid.R;
import org.openhab.habdroid.model.topview.StringUtil;

/**
 * Created by staufferr on 10.10.2014.
 */
public class SVGImageViewFactory {
    private Context context;

    public SVGImageViewFactory(Context context) {
        if (context == null) {
            throw new NullPointerException("context must not be undefined!");
        }

        this.context = context;
    }

    public ImageView createFromAsset(final AssetManager assetManager, final String fileName) {
        if (assetManager == null) {
            throw new NullPointerException("assetManager must not be undefined!");
        }
        if (StringUtil.isStringUndefinedOrEmpty(fileName)) {
            throw new IllegalArgumentException("fileName must not be undefined or empty!");
        }

        final ImageView imageView = new ImageView(context);

        imageView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                // Now the image view knows its actual size
                int viewWidth = view.getWidth();
                int viewHeight = view.getHeight();

                // Create bitmap of that size and
                Bitmap bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);

                // Clear background to white
                canvas.drawRGB(255, 255, 255);

                try {
                    SVG svg = SVG.getFromAsset(assetManager, fileName);
                    float svgWidth = svg.getDocumentWidth();
                    float svgHeight = svg.getDocumentHeight();

                    svg.renderToCanvas(canvas); // Does not scale the SVG image!
                } catch (Exception e) {
                    throw new RuntimeException("MARKER 111");
                }

                imageView.setImageBitmap(bitmap);
            }
        });

        return imageView;
    }
}
