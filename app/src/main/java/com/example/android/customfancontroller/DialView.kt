package com.example.android.customfancontroller

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private  enum class FanSpeed(val label: Int) {
    OFF(R.string.fan_off),
    LOW(R.string.fan_low),
    MEDIUM(R.string.fan_medium),
    HIGH(R.string.fan_high);

    fun next() = when (this) {
        OFF -> LOW
        LOW -> MEDIUM
        MEDIUM -> HIGH
        HIGH -> OFF
    }
}

private const val RADIUS_OFFSET_LABEL = 30
private const val RADIUS_OFFSET_INDICATOR = -35

// CUSTOM VIEW KOJI KREIRAM
class DialView @JvmOverloads constructor(context: Context,
                                         attributeSet: AttributeSet? = null,
                                         defStyleAttr: Int = 0
                                        ) : View(context, attributeSet, defStyleAttr)  {


    private var radius = 0.0f
    private var fanSpeed = FanSpeed.OFF
    private val pointPosition: PointF = PointF(0.0f, 0.0f)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    // da bih imao vezu sa custom attributima iz filea attrs.xml
    private var fanSpeedLowColor = 0
    private var fanSpeedMediumColor = 0
    private var fanSpeedMaxColor = 0

    // Da bi view moga da bude klikabilan
    init {
        isClickable = true

        // veza sa custom atributima
        context.withStyledAttributes(set = attributeSet, attrs = R.styleable.DialView) {
            fanSpeedLowColor = getColor(R.styleable.DialView_fanColor1, 0)
            fanSpeedMediumColor = getColor(R.styleable.DialView_fanColor2, 0)
            fanSpeedMaxColor = getColor(R.styleable.DialView_fanColor3, 0)
        }
    }


    // Moras nekoliko metoda da overridujes da bi mogao da koristis svoj custom view
    // onSizeChanged() - da bi se izracunala velicina view kada se prvi put pojavi, kao i svaki put kada mu se velicina promeni
    // onDraw() - da bi se view iscrtao koristeci Canavas, objekat stilizovan Paintom; ovu metodu sto manje opterecivati inicijalizacijama
    // invalidate() - koja reaguje na korisnikovove klikove i menja kako se view iscrtava, pozivanjem onDraw() metode

    // Ima korisnik metoda koje mogu posuziti za:
    // - crtanje texta - drawText()
    // - podesavanje typefacea - setTypeface()
    // - boja texta - setColor()
    // - crtanje pravougaonika - drawRect()
    // - crtanje elipsa - drawOval()
    // - crtanje lukova - drawArc()
    // - menjati boje unutrasnjosti i okvira oblika - setStyle()
    // - crtanje bitmapa - drawBitmap()


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = (min(width, height) / 2.0 * 0.8).toFloat()
    }


    private fun PointF.computeXYForSpeed(pos: FanSpeed, radius: Float) {
        // Uglvi su u radijanima
        val startAngle = Math.PI * (9/8.0)
        val angle = startAngle + pos.ordinal * (Math.PI / 4)
        x = (radius * cos(angle)).toFloat() + width / 2
        y = (radius * sin(angle)).toFloat() + height / 2
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // podesavam boju view u zavisnosti da li je ukljucen ili iskljucen ventilator
        paint.color = when(fanSpeed) {
            FanSpeed.OFF -> Color.GRAY
            FanSpeed.LOW -> fanSpeedLowColor
            FanSpeed.MEDIUM -> fanSpeedMediumColor
            FanSpeed.HIGH -> fanSpeedMaxColor
        }

        // dodajem da bih iscrtao view
        canvas?.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)

        // dodajem da bih iscrtao krug za brojcanik
        val markerRadius = radius + RADIUS_OFFSET_INDICATOR
        pointPosition.computeXYForSpeed(fanSpeed, markerRadius)
        paint.color = Color.BLACK
        canvas?.drawCircle(pointPosition.x, pointPosition.y, radius / 12, paint)

        // dodajem da bih iscrtao tekst brojcanika
        val labelRadius = radius + RADIUS_OFFSET_LABEL
        for (i in FanSpeed.values()) {
            pointPosition.computeXYForSpeed(i, labelRadius)
            val label = resources.getString(i.label)
            canvas?.drawText(label, pointPosition.x, pointPosition.y, paint)
        }
    }


    override fun performClick(): Boolean {
        if (super.performClick()) return true

        fanSpeed = fanSpeed.next()

        contentDescription = resources.getString(fanSpeed.label)

        invalidate()
        return true
    }

}