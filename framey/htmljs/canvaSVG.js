/*****
*
*   CanvaSVG.es
*
*   Author: Antoine Quint
*
*****/

var xhtml_ns = 'http://www.w3.org/1999/xhtml';
var svg_ns = 'http://www.w3.org/2000/svg';
var xlink_ns = 'http://www.w3.org/1999/xlink';

CanvaSVG.VERSION = 0.1;

function CanvaSVG () {
	var svgs = document.getElementsByTagNameNS(svg_ns, 'svg');
	if (svgs.length > 0) {
		for (var i=0; i<svgs.length; i++) {
			var svg = svgs[i];
			new CanvaSVGDocument(svg);
		}
	}
};

CanvaSVG.LogWarning = function (msg, obj) {
	// do something with warnings
};

CanvaSVG.LogError = function (msg, obj) {
	alert('Error on ' + obj + '\n' + msg);
};

/*****
*
*   CanvaSVGDocument.es
*
*   Author: Antoine Quint
*
*****/

/*****
*
*		TODO:
*
*			- write getColor() method for paint server and opacity
*			- write getPaintServers() as initial parsing method
*			- this.CurrentColor for "color" attribute
*			- stop using 2DContext.globalApha, except for gradients fill-opacity
*
*****/

CanvaSVGDocument.VERSION = 0.1;

function CanvaSVGDocument (svgElement) {
	this.Root = svgElement;
	this.Canvas = null;
	this.Context = null;
	this.Inheritance = [];
	this.IDs = [];
	//
	this.Fill = 'black';
	this.FillOpacity = 1;
	this.Stroke = 'none';
	this.StrokeWidth = 1;
	this.StrokeOpacity = 1;
	this.Transform = false;
	//
	this.LineCap = 'butt';
	this.LineJoin = 'miter';
	this.MiterLimit = 4;
	//
	this.CurrentColor = 'black';
	//
	this.PaintServers = [];
	//
	this.SetupCanvas();
	this.Parse(this.Root);
};

CanvaSVGDocument.prototype.SetupCanvas = function () {
	var w = this.Root.getAttribute('width');
	var h = this.Root.getAttribute('height');
	var canvas = document.createElementNS(xhtml_ns, 'canvas');
	canvas.setAttribute('width', w);
	canvas.setAttribute('height', h);
	this.Canvas = this.Root.parentNode.appendChild(canvas);
	this.Context = this.Canvas.getContext('2d');
	this.Context.clearRect(0,0,w,h);
	// handle viewBox
	if (this.Root.hasAttribute('viewBox')) {
		var viewBox = new ViewBox(this.Root);
		var transforms = viewBox.GetTransformInstructions();
		while (transforms.length) {
			var t = transforms.pop();
			if (t.Type == 'scale') {
				// watch out for 0
				this.Context.scale(1/t.Sx, 1/t.Sy);
			} else if (t.Type == 'translate') {
				this.Context.translate(-t.Tx, -t.Ty);
			}
		}
	}
	// handle viewport-fill
	// TODO: report gradients as error
	if (this.Root.hasAttribute('viewport-fill')) {
		var fill_color = this.Root.getAttribute('viewport-fill');
		var fill_opacity = this.Root.hasAttribute('viewport-fill-opacity') ? this.Root.getAttribute('viewport-fill-opacity') : 1;
		this.Context.fillStyle = this.GetColorAsRGBA(fill_color, fill_opacity);
		this.Context.fillRect(0,0,w,h);
	}
};

/*****
*
*   Global Parsing
*
*****/

// might want to try with nextSibling for performance
CanvaSVGDocument.prototype.Parse = function (element) {
	this.PreProcessContainer(element);
	var children = element.childNodes;
	for (var i=0; i<children.length; i++) {
		var node = children.item(i);
		if (node.nodeType == 1) { // node is an element
			var display = node.hasAttribute('display') ? node.getAttribute('display') : 'inline';
			var visibility = node.hasAttribute('visibility') ? node.getAttribute('visibility') : 'visible';
			if (display != 'none' && visibility != 'hidden' && visibility != 'collapsed') {
				this.ParseElement(node);
			}
		}
	}
	this.PostProcessContainer(element);
};

CanvaSVGDocument.prototype.PreProcessContainer = function (element) {

	var transforms;
	
	if (this.Inheritance.length == 0) {

		this.Fill = element.hasAttribute('fill') ? element.getAttribute('fill') : this.Fill;
		this.FillOpacity = element.hasAttribute('fill-opacity') ? element.getAttribute('fill-opacity') : this.FillOpacity;
		this.Stroke = element.hasAttribute('stroke') ? element.getAttribute('stroke') : this.Stroke;
		this.StrokeWidth = element.hasAttribute('stroke-width') ? element.getAttribute('stroke-width') : this.StrokeWidth;
		this.StrokeOpacity = element.hasAttribute('stroke-opacity') ? element.getAttribute('stroke-opacity') : this.StrokeOpacity;
		this.LineCap = element.hasAttribute('stroke-linecap') ? element.getAttribute('stroke-linecap') : this.LineCap;
		this.LineJoin = element.hasAttribute('stroke-linejoin') ? element.getAttribute('stroke-linejoin') : this.LineJoin;
		this.MiterLimit = element.hasAttribute('stroke-miterlimit') ? element.getAttribute('stroke-miterlimit') : this.MiterLimit;
		transforms = false;

	} else {

		var parent = this.Inheritance[this.Inheritance.length - 1];
		this.Fill = element.hasAttribute('fill') ? element.getAttribute('fill') : parent.Fill;
		this.FillOpacity = element.hasAttribute('fill-opacity') ? element.getAttribute('fill-opacity') : parent.FillOpacity;
		this.Stroke = element.hasAttribute('stroke') ? element.getAttribute('stroke') : parent.Stroke;
		this.StrokeWidth = element.hasAttribute('stroke-width') ? element.getAttribute('stroke-width') : parent.StrokeWidth;
		this.StrokeOpacity = element.hasAttribute('stroke-opacity') ? element.getAttribute('stroke-opacity') : parent.StrokeOpacity;
		this.LineCap = element.hasAttribute('stroke-linecap') ? element.getAttribute('stroke-linecap') : parent.LineCap;
		this.LineJoin = element.hasAttribute('stroke-linejoin') ? element.getAttribute('stroke-linejoin') : parent.LineJoin;
		this.MiterLimit = element.hasAttribute('stroke-miterlimit') ? element.getAttribute('stroke-miterlimit') : parent.MiterLimit;

		if (element.hasAttribute('transform')) {
			transforms = this.GetTransformInstructions(element.getAttribute('transform'));
			for (var i=0; i<transforms.length; i++) {
				var t = transforms[i];
				if (t.Type == 'scale') {
					this.Context.scale(t.Sx, t.Sy);			
				} else if (t.Type == 'translate') {
					this.Context.translate(t.Tx, t.Ty);
				} else if (t.Type == 'rotate') {
					this.Context.rotate(t.Angle);
				}
			}
		}
	}

	try {
		this.Context.fillStyle = this.Fill;
	} catch(e) {
		//
	}

	if (this.Stroke !== 'none') {
		try {
			this.Context.strokeStyle = stroke;
		} catch(e) {
			//alert(stroke);
		}
	}

	this.Context.lineWidth = this.StrokeWidth;
	this.Context.lineCap = this.LineCap;
	this.Context.lineJoin = this.LineJoin;
	this.Context.miterLimit = this.MiterLimit;

	this.Inheritance.push({
		Element : element,
		Fill : this.Fill,
		FillOpacity : this.FillOpacity,
		Stroke : this.Stroke,
		StrokeWidth : this.StrokeWidth,
		StrokeOpacity : this.StrokeOpacity,
		LineCap: this.LineCap,
		LineJoin: this.LineJoin,
		MiterLimit: this.MiterLimit,
		Transforms : transforms
	});

};

CanvaSVGDocument.prototype.PostProcessContainer = function (element) {
	var current = this.Inheritance.pop();
	var transforms = current.Transforms;

	if (this.Inheritance.length) {
		var parent = this.Inheritance[this.Inheritance.length - 1];
		this.Fill = parent.Fill;
		try {
			this.Context.fillStyle = fill;
		} catch(e) {
			//
		}
		this.Stroke = parent.Stroke;
		try {
			this.Context.strokeStyle = this.Stroke;
		} catch(e) {
			//alert(stroke);
		}
	}

	// revert transforms
	if (element.hasAttribute('transform')) {
		while (transforms.length) {
			var t = transforms.pop();
			if (t.Type == 'scale') {
				// watch out for 0
				this.Context.scale(1/t.Sx, 1/t.Sy);			
			} else if (t.Type == 'translate') {
				this.Context.translate(-t.Tx, -t.Ty);
			} else if (t.Type == 'rotate') {
				this.Context.rotate(-t.Angle);
			}
		}
	}
	
};

CanvaSVGDocument.prototype.ParseElement = function (element) {
	if (element.namespaceURI == svg_ns) { // element in SVG namespace
		if (element.hasAttribute('id')) {
			this.IDs.push({
				Id : element.getAttribute('id'),
				Element : element
			});
		}
		var name = element.localName;
		var handler = 'Parse_' + name;
		if (this[handler] == null) { // check if we can handle that element
			CanvaSVG.LogError('SVG element not implemented', element);
		} else {
			if (name == 'g' || name == 'defs') {
				this.Parse(element);
			} else if (name == 'linearGradient' || name == 'radialGradient') {
				this[handler](element);
			} else {
				this.Draw(element, handler);
			}
		}
	} else {
		CanvaSVG.LogWarning('Element unknown', element);	
	}
};

CanvaSVGDocument.prototype.Parse_g = function (group) {
	this.Parse(group);
};

CanvaSVGDocument.prototype.Draw = function (element, handler) {

	var transforms = null;
	if (element.hasAttribute('transform')) {
		transforms = this.GetTransformInstructions(element.getAttribute('transform'));
		for (var i=0; i<transforms.length; i++) {
			var t = transforms[i];
			if (t.Type == 'scale') {
				this.Context.scale(t.Sx, t.Sy);			
			} else if (t.Type == 'translate') {
				this.Context.translate(t.Tx, t.Ty);
			} else if (t.Type == 'rotate') {
				this.Context.rotate(t.Angle);
			}
		}
	}

	if (element.hasAttribute('fill')) {
		this.Fill = element.getAttribute('fill');
		if (this.Fill.substr(0,5) == 'url(#') {
			var paint_server_id = this.Fill.substr(5,this.Fill.length-6);
			// lookup paint server in IDs table
			for (var i=0; i<	this.PaintServers.length; i++) {
				if (this.PaintServers[i].Id == paint_server_id) {
					this.Fill = this.PaintServers[i].Paint;
				}
			}
		}
		try {
			this.Context.fillStyle = this.Fill;
		} catch(e) {
			//alert(stroke);
		}
	}

	if (element.hasAttribute('stroke-width')) {
		this.StrokeWidth = element.getAttribute('stroke-width');
		this.Context.lineWidth = this.StrokeWidth;
	}

	if (element.hasAttribute('stroke')) {
		this.Stroke = element.getAttribute('stroke');
		try {
			this.Context.strokeStyle = this.Stroke;
		} catch(e) {
			//alert(stroke);
		}
	}

	// lineCap	
	if (element.hasAttribute('stroke-linecap')) {
		this.LineCap = element.getAttribute('stroke-linecap');
		try {
			this.Context.lineCap = this.LineCap;
		} catch(e) {
			//
		}
	}

	// lineJoin	
	if (element.hasAttribute('stroke-linejoin')) {
		this.LineJoin = element.getAttribute('stroke-linejoin');
		try {
			this.Context.lineJoin = this.LineJoin;
		} catch(e) {
			//
		}
	}

	// miterLimit	
	if (element.hasAttribute('stroke-miterlimit')) {
		this.MiterLimit = Number(element.getAttribute('miterlimit'));
		try {
			this.Context.miterLimit = this.MiterLimit;
		} catch(e) {
			//
		}
	}
	
	var parent = this.Inheritance[this.Inheritance.length - 1];

	if (element.hasAttribute('fill-opacity')) {
		this.Context.globalAlpha = element.getAttribute('fill-opacity');
	} else {
		this.Context.globalAlpha = parent.FillOpacity;	
	}

	// draws fill if any
	if (this.Fill !== 'none') {
		//alert('filling ' + element.nodeName + ' with ' + this.Fill);
		this[handler](element);
		this.Context.fill();
	}

	// revert to previous fill if needed
	if (this.Fill !== parent.Fill) {
		this.Fill = parent.Fill;
		this.Context.fillStyle = this.Fill;
	}

	if (element.hasAttribute('stroke-opacity')) {
		this.Context.globalAlpha = element.getAttribute('stroke-opacity');
	} else {
		this.Context.globalAlpha = parent.StrokeOpacity;	
	}
	
	// draws stroke if any
	if (this.Stroke != 'none') {
		this[handler](element);
		this.Context.stroke();
	}

	// revert to previous stroke if needed
	if (this.Stroke != parent.Stroke) {
		this.Stroke = parent.Stroke;
		try {
			this.Context.strokeStyle = this.Stroke;
		} catch(e) {
			//alert(stroke);
		}
	}

	// revert to previous stroke-width
	if (this.StrokeWidth != parent.StrokeWidth) {
		this.StrokeWidth = parent.StrokeWidth;
		this.Context.lineWidth = this.StrokeWidth;
	}

	// lineCap	
	if (this.LineCap != parent.LineCap) {
		this.LineCap = parent.LineCap;
		this.Context.lineCap = this.LineCap;
	}

	// lineJoin	
	if (this.LineJoin != parent.LineJoin) {
		this.LineJoin = parent.LineJoin;
		this.Context.lineJoin	= this.LineJoin;
	}

	// miterLimit	
	if (this.MiterLimit != parent.MiterLimit) {
		this.MiterLimit = parent.MiterLimit;
		this.Context.miterLimit = this.MiterLimit;
	}

	// revert transforms
	if (element.hasAttribute('transform')) {
		while (transforms.length) {
			var t = transforms.pop();
			if (t.Type == 'scale') {
				// watch out for 0
				this.Context.scale(1/t.Sx, 1/t.Sy);			
			} else if (t.Type == 'translate') {
				this.Context.translate(-t.Tx, -t.Ty);
			} else if (t.Type == 'rotate') {
				this.Context.rotate(-t.Angle);
			}
		}
	}

};

CanvaSVGDocument.prototype.GetTransformInstructions = function (transform) {
	var instructions = [];
	var parser = new TransformParser();
	parser.setHandler(new TransformParserHandler(instructions));
	try {
		parser.parseTransform(transform);
	} catch(e) {
		println("Parse failed: " + e.message);
	}
	return instructions;
};

/*****
*
*   Renderable Elements Parsing
*
*****/

// need to match cx and cy in case one is missing and clip values if great than half w/h
CanvaSVGDocument.prototype.Parse_rect = function (rect) {
	// get specified geometry attributes' values or revert to default
	var w = rect.hasAttribute('width') ? Number(rect.getAttribute('width')) : 0;
	var h = rect.hasAttribute('height') ? Number(rect.getAttribute('height')) : 0;
	var x = rect.hasAttribute('x') ? Number(rect.getAttribute('x')) : 0;
	var y = rect.hasAttribute('y') ? Number(rect.getAttribute('y')) : 0;
	var rx = rect.hasAttribute('rx') ? Number(rect.getAttribute('rx')) : 0;
	var ry = rect.hasAttribute('ry') ? Number(rect.getAttribute('ry')) : 0;
	// check width and height to be negative
	if (w < 0 || h < 0) {
		CanvaSVG.LogError('Attributes "width" and "height" must have a positive value', rect);
		return;	
	}
	// check width and height to be zero
	if (w == 0 || h == 0) {
		CanvaSVG.LogWarning('Attributes "width" or "height" with value zero disable rendering', rect);
		return;
	}
	// check rx and ry values to match if one is 0
	if ((rx == 0 || ry == 0) && !(rx == 0 && ry == 0)) {
		rx = (rx == 0) ? ry : rx;
		ry = (ry == 0) ? rx : ry;
	}
	if (rx != 0) {	// draw rounded rectangle
		this.DrawRoundedRect(x,y,w,h,rx,ry);
	} else {	// draw regular rectangle
		this.DrawRect(x,y,w,h);
	}
};

CanvaSVGDocument.prototype.Parse_circle = function (circle) {
	// get specified geometry attributes' values or revert to default
	var cx = circle.hasAttribute('cx') ? Number(circle.getAttribute('cx')) : 0;
	var cy = circle.hasAttribute('cy') ? Number(circle.getAttribute('cy')) : 0;
	var r = circle.hasAttribute('r') ? Number(circle.getAttribute('r')) : 0;
	// check radius to be negative
	if (r < 0) {
		CanvaSVG.LogError('Attribute "r" must have a positive value', circle);
		return;	
	}
	// check radius to be zero
	if (r == 0) {
		CanvaSVG.LogWarning('Attribute "r" with value zero disables rendering', circle);
		return;
	}
	// draw circle
	this.DrawCircle(cx,cy,r);
};

CanvaSVGDocument.prototype.Parse_ellipse = function (ellipse) {
	// get specified geometry attributes' values or revert to default
	var cx = ellipse.hasAttribute('cx') ? Number(ellipse.getAttribute('cx')) : 0;
	var cy = ellipse.hasAttribute('cy') ? Number(ellipse.getAttribute('cy')) : 0;
	var rx = ellipse.hasAttribute('rx') ? Number(ellipse.getAttribute('rx')) : 0;
	var ry = ellipse.hasAttribute('ry') ? Number(ellipse.getAttribute('ry')) : 0;
	// check rx and ry to be negative
	if (rx < 0 || ry < 0) {
		CanvaSVG.LogError('Attributes "rx" and "ry" must have a positive value', ellipse);
		return;	
	}
	// check width and height to be zero
	if (rx == 0 || ry == 0) {
		CanvaSVG.LogWarning('Attributes "rx" or "ry" with value zero disable rendering', ellipse);
		return;
	}
	// draw ellipse
	this.DrawEllipse(cx,cy,rx,ry);
};

CanvaSVGDocument.prototype.Parse_line = function (line) {
	var x1 = ellipse.hasAttribute('x1') ? Number(ellipse.getAttribute('x1')) : 0;
	var y1 = ellipse.hasAttribute('y1') ? Number(ellipse.getAttribute('y1')) : 0;
	var x2 = ellipse.hasAttribute('x2') ? Number(ellipse.getAttribute('x2')) : 0;
	var y2 = ellipse.hasAttribute('y2') ? Number(ellipse.getAttribute('y2')) : 0;
	this.DrawLine(x1, y1, x2, y2);
};

CanvaSVGDocument.prototype.Parse_polyline = function (polyline) {
	var points = polyline.getAttribute('points').split(/[\s,]+/);
	this.Context.beginPath();
	this.Context.moveTo(Number(points[0]),Number(points[1]));
	for (var i=2; i<points.length; i+=2) {
		this.Context.lineTo(Number(points[i]),Number(points[i+1]));
	}
};

CanvaSVGDocument.prototype.Parse_polygon = function (polygon) {
	var points = polygon.getAttribute('points').split(/[\s,]+/);
	this.Context.beginPath();
	this.Context.moveTo(Number(points[0]),Number(points[1]));
	for (var i=2; i<points.length; i+=2) {
		this.Context.lineTo(Number(points[i]),Number(points[i+1]));
	}
	this.Context.closePath();
};

CanvaSVGDocument.prototype.Parse_path = function (path) {
	if (path.hasAttribute('d')) {
		d = path.getAttribute('d');
	} else {
		CanvaSVG.LogError('Attribute "d" undefined on <path>', path);
		return;
	}
	var parser = new PathParser();
	parser.setHandler(new PathParserHandler(this.Context));
	parser.parseData(d);
};

CanvaSVGDocument.prototype.Parse_defs = function (defs) {
	//this.Parse(defs);
};

CanvaSVGDocument.prototype.Parse_image = function (image) {
	var x = image.hasAttribute('x') ? Number(image.getAttribute('x')) : 0;
	var y = image.hasAttribute('y') ? Number(image.getAttribute('y')) : 0;
	// TODO: report errors
	var width = image.hasAttribute('width') ? Number(image.getAttribute('width')) : null;
	var height = image.hasAttribute('height') ? Number(image.getAttribute('height')) : null;
	//
	var img = new Image(width, height);
	img.src = image.getAttributeNS(xlink_ns, 'href');
	this.Context.drawImage(img, x, y);
};

/*****
*
*   Paint Servers
*
*****/

CanvaSVGDocument.prototype.Parse_linearGradient = function (linearGradient) {
	var x1 = linearGradient.hasAttribute('x1') ? Number(linearGradient.getAttribute('x1')) : 0;
	var y1 = linearGradient.hasAttribute('y1') ? Number(linearGradient.getAttribute('y1')) : 0;
	var x2 = linearGradient.hasAttribute('x2') ? Number(linearGradient.getAttribute('x2')) : 1;
	var y2 = linearGradient.hasAttribute('y2') ? Number(linearGradient.getAttribute('y2')) : 0;
	var id = linearGradient.hasAttribute('id') ? linearGradient.getAttribute('id') : 'gradient_' + (this.PaintServers.length+1);
	var gradient = this.Context.createLinearGradient(x1,y1,x2,y2);
	var stops = linearGradient.getElementsByTagNameNS(svg_ns, 'stop');
	for (var i=0; i<stops.length; i++) {
		var stop = stops.item(i);
		var offset = stop.hasAttribute('offset') ? Number(stop.getAttribute('offset')) : 0;
		var stop_color = stop.hasAttribute('stop-color') ? stop.getAttribute('stop-color') : 'black';
		var stop_opacity = stop.hasAttribute('stop-opacity') ? stop.getAttribute('stop-opacity') : 1;
		gradient.addColorStop(offset, this.GetColorAsRGBA(stop_color, stop_opacity));
	}
	this.PaintServers.push({
		Id: id,
		Type: 'gradient',
		Paint: gradient
	});
};

CanvaSVGDocument.prototype.Parse_radialGradient = function (radialGradient) {
	var cx = radialGradient.hasAttribute('cx') ? Number(radialGradient.getAttribute('cx')) : 0;
	var cy = radialGradient.hasAttribute('cy') ? Number(radialGradient.getAttribute('cy')) : 0;
	var r = radialGradient.hasAttribute('r') ? Number(radialGradient.getAttribute('r')) : 1;
	var id = radialGradient.hasAttribute('id') ? radialGradient.getAttribute('id') : 'gradient_' + (this.PaintServers.length+1);
	var gradient = this.Context.createRadialGradient(cx, cy, 0, cx, cy, r);
	var stops = radialGradient.getElementsByTagNameNS(svg_ns, 'stop');
	for (var i=0; i<stops.length; i++) {
		var stop = stops.item(i);
		var offset = stop.hasAttribute('offset') ? Number(stop.getAttribute('offset')) : 0;
		var stop_color = stop.hasAttribute('stop-color') ? stop.getAttribute('stop-color') : 'black';
		var stop_opacity = stop.hasAttribute('stop-opacity') ? stop.getAttribute('stop-opacity') : 1;
		gradient.addColorStop(offset, this.GetColorAsRGBA(stop_color, stop_opacity));
	}
	this.PaintServers.push({
		Id: id,
		Type: 'gradient',
		Paint: gradient
	});
};

CanvaSVGDocument.prototype.Parse_solidColor = function (solidColor) {
	var solid_color = solidColor.hasAttribute('solid-color') ? solidColor.getAttribute('solid-color') : black;
	var solid_opacity = solidColor.hasAttribute('solid-opacity') ? Number(solidColor.getAttribute('solid-opacity')) : 1;
	var id = solidColor.hasAttribute('id') ? solidColor.getAttribute('id') : 'color_' + (this.PaintServers.length+1);
	var color = this.GetColorAsRGBA(solid_color, solid_opacity);
	this.PaintServers.push({
		Id: id,
		Type: 'color',
		Paint: color
	});
};


/*****
*
*   Drawing Methods
*
*****/

CanvaSVGDocument.prototype.DrawRect = function (x,y,w,h) {
	this.Context.beginPath();
	this.Context.rect(x,y,w,h);
	this.Context.closePath();
};

CanvaSVGDocument.prototype.DrawRoundedRect = function (x,y,w,h,rx,ry) {
	this.Context.beginPath();
	this.Context.moveTo(x+rx,y);
	this.Context.lineTo(x+w-rx,y);
	this.Context.bezierCurveTo(x+w,y,x+w,y+ry,x+w,y+ry);
	this.Context.lineTo(x+w,y+h-ry);
	this.Context.bezierCurveTo(x+w,y+h,x+w-rx,y+h,x+w-rx,y+h);
	this.Context.lineTo(x+rx,y+h);
	this.Context.bezierCurveTo(x,y+h,x,y+h-ry,x,y+h-ry);
	this.Context.lineTo(x,y+ry);
	this.Context.bezierCurveTo(x,y,x+rx,y,x+rx,y);
	this.Context.closePath();
};

CanvaSVGDocument.prototype.DrawCircle = function (cx,cy,r) {
	this.Context.beginPath();
	this.Context.arc(cx,cy,r,0,2*Math.PI,1);
	this.Context.closePath();
};

CanvaSVGDocument.prototype.DrawEllipse = function (cx,cy,rx,ry) {
	var r = rx > ry ? rx : ry;
	var sx = rx > ry ? 1 : rx/ry;
	var sy = rx > ry ? ry/rx : 1;
	this.Context.translate(cx,cy);
	this.Context.scale(sx,sy);
	this.Context.beginPath();
	this.Context.arc(0,0,r,0,2*Math.PI,1);
	this.Context.closePath();
	this.Context.scale(1/sx,1/sy);
	this.Context.translate(-cx,-cy);
	this.Context.closePath();
};

CanvaSVGDocument.prototype.DrawLine = function (x1, y1, x2, y2) {
	this.Context.beginPath();
	this.Context.moveTo(x1,y1);
	this.Context.lineTo(x2, y2);
};

CanvaSVGDocument.prototype.DrawPolyline = function (points) {
	var start = points.shift();
	this.Context.beginPath();
	this.Context.moveTo(start[O],start[1]);
	while (points.length) {
		var point = points.shift();
		this.Context.moveTo(point[O],point[1]);		
	}
};

CanvaSVGDocument.prototype.DrawPolygon = function (points) {
	this.DrawPolyline(points);
	this.Context.closePath();
};

/*****
*
*   Color Methods
*
*****/

CanvaSVGDocument.prototype.GetColorAsRGBA = function (color, opacity) {
	var result = false;
	var rgba = false;
	if (color.substr(0,4) == 'rgb(') { // rgb() color
		if (/%/.test(color)) { // using % 
			rgba = color.substr(4,color.length-5).split(/[\s,]+/);
			rgba[0] = Math.round((parseFloat(rgba[0])/100) * 255);
			rgba[1] = Math.round((parseFloat(rgba[1])/100) * 255);
			rgba[2] = Math.round((parseFloat(rgba[2])/100) * 255);
			rgba.push(opacity);
		} else { // using ints
			rgba = color.substr(4,color.length-5).split(/[\s,]+/);
			rgba.push(opacity);
		}
	} else if ((color.charAt(0) == '#') && (color.length == 4)) { // #xxx
		rgba = [];
		rgba.push(this.ConvertFromHexa(color.charAt(1) + color.charAt(1)));
		rgba.push(this.ConvertFromHexa(color.charAt(2) + color.charAt(2)));
		rgba.push(this.ConvertFromHexa(color.charAt(3) + color.charAt(3)));
		rgba.push(opacity);
	} else if ((color.charAt(0) == '#') && (color.length == 7)) { // #xxxxxx
		rgba = [];
		rgba.push(this.ConvertFromHexa(color.charAt(1) + color.charAt(2)));
		rgba.push(this.ConvertFromHexa(color.charAt(3) + color.charAt(4)));
		rgba.push(this.ConvertFromHexa(color.charAt(5) + color.charAt(6)));
		rgba.push(opacity);
	} else if (color.substr(0,5) == 'url(#') { // paint server
		// check if solidColor
	} else { // maybe keyword
		if (CanvaSVGDocument.COLORS[color] != null) {
			rgba = CanvaSVGDocument.COLORS[color];
			rgba.push(opacity);
		}
	} 
	if (rgba) {
		result = 'rgba(' + rgba[0] + ',' + rgba[1] + ',' + rgba[2] + ',' + rgba[3] + ')';
	}
	return result;
};

// TODO: implement
CanvaSVGDocument.prototype.IsGradient = function (color) {
	
}

CanvaSVGDocument.prototype.ConvertFromHexa = function (hexa) {
	function getHexa (c) {
		var result = false;
		if (c == 'a') {
			result = 10;
		} else if (c == 'b') {
			result = 11;
		} else if (c == 'c') {
			result = 12;
		} else if (c == 'd') {
			result = 13;
		} else if (c == 'e') {
			result = 14;
		} else if (c == 'f') {
			result = 15;
		} else { 
			result = Number(c);
		}
		return result;
	}
	return getHexa(hexa.charAt(0)) * 16 + getHexa(hexa.charAt(1));
};

CanvaSVGDocument.COLORS = {
	aliceblue: [240, 248, 255],
	antiquewhite: [250, 235, 215],
	aqua: [0, 255, 255],
	aquamarine: [127, 255, 212],
	azure: [240, 255, 255],
	beige: [245, 245, 220],
	bisque: [255, 228, 196],
	black: [0, 0, 0],
	blanchedalmond: [255, 235, 205],
	blue: [0, 0, 255],
	blueviolet: [138, 43, 226],
	brown: [165, 42, 42],
	burlywood: [222, 184, 135],
	cadetblue: [95, 158, 160],
	chartreuse: [127, 255, 0],
	chocolate: [210, 105, 30],
	coral: [255, 127, 80],
	cornflowerblue: [100, 149, 237],
	cornsilk: [255, 248, 220],
	crimson: [220, 20, 60],
	cyan: [0, 255, 255],
	darkblue: [0, 0, 139],
	darkcyan: [0, 139, 139],
	darkgoldenrod: [184, 134, 11],
	darkgray: [169, 169, 169],
	darkgreen: [0, 100, 0],
	darkgrey: [169, 169, 169],
	darkkhaki: [189, 183, 107],
	darkmagenta: [139, 0, 139],
	darkolivegreen: [85, 107, 47],
	darkorange: [255, 140, 0],
	darkorchid: [153, 50, 204],
	darkred: [139, 0, 0],
	darksalmon: [233, 150, 122],
	darkseagreen: [143, 188, 143],
	darkslateblue: [72, 61, 139],
	darkslategray: [47, 79, 79],
	darkslategrey: [47, 79, 79],
	darkturquoise: [0, 206, 209],
	darkviolet: [148, 0, 211],
	deeppink: [255, 20, 147],
	deepskyblue: [0, 191, 255],
	dimgray: [105, 105, 105],
	dimgrey: [105, 105, 105],
	dodgerblue: [30, 144, 255],
	firebrick: [178, 34, 34],
	floralwhite: [255, 250, 240],
	forestgreen: [34, 139, 34],
	fuchsia: [255, 0, 255],
	gainsboro: [220, 220, 220],
	ghostwhite: [248, 248, 255],
	gold: [255, 215, 0],
	goldenrod: [218, 165, 32],
	gray: [128, 128, 128],
	grey: [128, 128, 128],
	green: [0, 128, 0],
	greenyellow: [173, 255, 47],
	honeydew: [240, 255, 240],
	hotpink: [255, 105, 180],
	indianred: [205, 92, 92],
	indigo: [75, 0, 130],
	ivory: [255, 255, 240],
	khaki: [240, 230, 140],
	lavender: [230, 230, 250],
	lavenderblush: [255, 240, 245],
	lawngreen: [124, 252, 0],
	lemonchiffon: [255, 250, 205],
	lightblue: [173, 216, 230],
	lightcoral: [240, 128, 128],
	lightcyan: [224, 255, 255],
	lightgoldenrodyellow: [250, 250, 210],
	lightgray: [211, 211, 211],
	lightgreen: [144, 238, 144],
	lightgrey: [211, 211, 211],
	lightpink: [255, 182, 193],
	lightsalmon: [255, 160, 122],
	lightseagreen: [32, 178, 170],
	lightskyblue: [135, 206, 250],
	lightslategray: [119, 136, 153],
	lightslategrey: [119, 136, 153],
	lightsteelblue: [176, 196, 222],
	lightyellow: [255, 255, 224],
	lime: [0, 255, 0],
	limegreen: [50, 205, 50],
	linen: [250, 240, 230],
	magenta: [255, 0, 255],
	maroon: [128, 0, 0],
	mediumaquamarine: [102, 205, 170],
	mediumblue: [0, 0, 205],
	mediumorchid: [186, 85, 211],
	mediumpurple: [147, 112, 219],
	mediumseagreen: [60, 179, 113],
	mediumslateblue: [123, 104, 238],
	mediumspringgreen: [0, 250, 154],
	mediumturquoise: [72, 209, 204],
	mediumvioletred: [199, 21, 133],
	midnightblue: [25, 25, 112],
	mintcream: [245, 255, 250],
	mistyrose: [255, 228, 225],
	moccasin: [255, 228, 181],
	navajowhite: [255, 222, 173],
	navy: [0, 0, 128],
	oldlace: [253, 245, 230],
	olive: [128, 128, 0],
	olivedrab: [107, 142, 35],
	orange: [255, 165, 0],
	orangered: [255, 69, 0],
	orchid: [218, 112, 214],
	palegoldenrod: [238, 232, 170],
	palegreen: [152, 251, 152],
	paleturquoise: [175, 238, 238],
	palevioletred: [219, 112, 147],
	papayawhip: [255, 239, 213],
	peachpuff: [255, 218, 185],
	peru: [205, 133, 63],
	pink: [255, 192, 203],
	plum: [221, 160, 221],
	powderblue: [176, 224, 230],
	purple: [128, 0, 128],
	red: [255, 0, 0],
	rosybrown: [188, 143, 143],
	royalblue: [65, 105, 225],
	saddlebrown: [139, 69, 19],
	salmon: [250, 128, 114],
	sandybrown: [244, 164, 96],
	seagreen: [46, 139, 87],
	seashell: [255, 245, 238],
	sienna: [160, 82, 45],
	silver: [192, 192, 192],
	skyblue: [135, 206, 235],
	slateblue: [106, 90, 205],
	slategray: [112, 128, 144],
	slategrey: [112, 128, 144],
	snow: [255, 250, 250],
	springgreen: [0, 255, 127],
	steelblue: [70, 130, 180],
	tan: [210, 180, 140],
	teal: [0, 128, 128],
	thistle: [216, 191, 216],
	tomato: [255, 99, 71],
	turquoise: [64, 224, 208],
	violet: [238, 130, 238],
	wheat: [245, 222, 179],
	white: [255, 255, 255],
	whitesmoke: [245, 245, 245],
	yellow: [255, 255, 0],
	yellowgreen: [154, 205, 50]
};

/*****
*
*   The contents of this file were written by Kevin Lindsey
*   copyright 2002-2003 Kevin Lindsey
*
*   This file was compacted by jscompact
*   A Perl utility written by Kevin Lindsey (kevin@kevlindev.com)
*
*****/

Svg.VERSION=1.0;
Svg.NAMESPACE="http://www.w3.org/2000/svg";
function Svg(){}
PathParser.PARAMCOUNT={A:7,C:6,H:1,L:2,M:2,Q:4,S:4,T:2,V:1,Z:0};
PathParser.METHODNAME={A:"arcAbs",a:"arcRel",C:"curvetoCubicAbs",c:"curvetoCubicRel",H:"linetoHorizontalAbs",h:"linetoHorizontalRel",L:"linetoAbs",l:"linetoRel",M:"movetoAbs",m:"movetoRel",Q:"curvetoQuadraticAbs",q:"curvetoQuadraticRel",S:"curvetoCubicSmoothAbs",s:"curvetoCubicSmoothRel",T:"curvetoQuadraticSmoothAbs",t:"curvetoQuadraticSmoothRel",V:"linetoVerticalAbs",v:"linetoVerticalRel",Z:"closePath",z:"closePath"}
function PathParser(){this._lexer=new PathLexer();this._handler=null;}
PathParser.prototype.parsePath=function(path){if(path==null||path.namespaceURI!=Svg.NAMESPACE||path.localName!="path")throw new Error("PathParser.parsePath: The first parameter must be an SVG path element");this.parseData(path.getAttributeNS(null,"d"));};
PathParser.prototype.parseData=function(pathData){if(typeof(pathData)!="string")throw new Error("PathParser.parseData: The first parameter must be a string");if(this._handler!=null&&this._handler.beginParse!=null)this._handler.beginParse();var lexer=this._lexer;lexer.setPathData(pathData);var mode="BOP";var token=lexer.getNextToken();while(!token.typeis(PathToken.EOD)){var param_count;var params=new Array();switch(token.type){case PathToken.COMMAND:if(mode=="BOP"&&token.text!="M"&&token.text!="m")throw new Error("PathParser.parseData: a path must begin with a moveto command");mode=token.text;param_count=PathParser.PARAMCOUNT[token.text.toUpperCase()];token=lexer.getNextToken();break;case PathToken.NUMBER:break;default:throw new Error("PathParser.parseData: unrecognized token type: "+token.type);}for(var i=0;i<param_count;i++){switch(token.type){case PathToken.COMMAND:throw new Error("PathParser.parseData: parameter must be a number: "+token.text);case PathToken.NUMBER:params[i]=token.text-0;break;default:throw new Errot("PathParser.parseData: unrecognized token type: "+token.type);}token=lexer.getNextToken();}if(this._handler!=null){var handler=this._handler;var method=PathParser.METHODNAME[mode];if(handler[method]!=null)handler[method].apply(handler,params);}if(mode=="M")mode="L";if(mode=="m")mode="l";}};
PathParser.prototype.setHandler=function(handler){this._handler=handler;};
PathLexer.VERSION=1.0;
function PathLexer(pathData){if(pathData==null)pathData="";this.setPathData(pathData);}
PathLexer.prototype.setPathData=function(pathData){if(typeof(pathData)!="string")throw new Error("PathLexer.setPathData: The first parameter must be a string");this._pathData=pathData;};
PathLexer.prototype.getNextToken=function(){var result=null;var d=this._pathData;while(result==null){if(d==null||d==""){result=new PathToken(PathToken.EOD,"");}else if(d.match(/^([ \t\r\n,]+)/)){d=d.substr(RegExp.$1.length);}else if(d.match(/^([AaCcHhLlMmQqSsTtVvZz])/)){result=new PathToken(PathToken.COMMAND,RegExp.$1);d=d.substr(RegExp.$1.length);}else if(d.match(/^(([-+]?[0-9]+(\.[0-9]*)?|[-+]?\.[0-9]+)([eE][-+]?[0-9]+)?)/)){result=new PathToken(PathToken.NUMBER,parseFloat(RegExp.$1));d=d.substr(RegExp.$1.length);}else{throw new Error("PathLexer.getNextToken: unrecognized path data "+d);}}this._pathData=d;return result;};
PathToken.UNDEFINED=0;
PathToken.COMMAND=1;
PathToken.NUMBER=2;
PathToken.EOD=3;
function PathToken(type,text){if(arguments.length>0){this.init(type,text);}}
PathToken.prototype.init=function(type,text){this.type=type;this.text=text;};
PathToken.prototype.typeis=function(type){return this.type==type;}/*****
*
*   PathParserHandler.es
*
*   Author: Antoine Quint
*
*****/

function PathParserHandler (context) {
	this.Context = context;
	this.CurrentPoint = [0,0];
	this.CurrentCenter = [0,0];
	this.FirstCommand = true;
};

PathParserHandler.prototype.arcAbs = function(rx, ry, xAxisRotation, largeArcFlag, sweepFlag, x, y) {
	alert('arcs not supported');
};

PathParserHandler.prototype.arcRel = function(rx, ry, xAxisRotation, largeArcFlag, sweepFlag, x, y) {
	alert('arcs not supported');
};

PathParserHandler.prototype.curvetoCubicAbs = function(x1, y1, x2, y2, x, y) {
	this.Context.bezierCurveTo(x1, y1, x2, y2, x, y);
	this.CurrentPoint = [x,y];
	this.CurrentCenter = [x2,y2];
};

PathParserHandler.prototype.curvetoCubicRel = function(x1, y1, x2, y2, x, y) {
	x1 += this.CurrentPoint[0];
	y1 += this.CurrentPoint[1];
	x2 += this.CurrentPoint[0];
	y2 += this.CurrentPoint[1];
	x += this.CurrentPoint[0];
	y += this.CurrentPoint[1];
	this.Context.bezierCurveTo(x1, y1, x2, y2, x, y);
	this.CurrentPoint = [x,y];
	this.CurrentCenter = [x2,y2];
};

PathParserHandler.prototype.curvetoCubicSmoothAbs = function(x2, y2, x, y) {
	var x1 = this.CurrentPoint[0] * 2 - this.CurrentCenter[0];
	var y1 = this.CurrentPoint[1] * 2 - this.CurrentCenter[1];
	this.Context.bezierCurveTo(x1, y1, x2, y2, x, y);
	this.CurrentPoint = [x,y];
	this.CurrentCenter = [x2,y2];
};

PathParserHandler.prototype.curvetoCubicSmoothRel = function(x2, y2, x, y) {
	var x1 = this.CurrentPoint[0] * 2 - this.CurrentCenter[0];
	var y1 = this.CurrentPoint[1] * 2 - this.CurrentCenter[1];
	x2 += this.CurrentPoint[0];
	y2 += this.CurrentPoint[1];
	x += this.CurrentPoint[0];
	y += this.CurrentPoint[1];
	this.Context.bezierCurveTo(x1, y1, x2, y2, x, y);
	this.CurrentPoint = [x,y];
	this.CurrentCenter = [x2,y2];
};

PathParserHandler.prototype.linetoHorizontalAbs = function(x) {
	var y = this.CurrentPoint[1];
	this.Context.lineTo(x,y);
	this.CurrentPoint = [x,y];
	this.CurrentCenter = [x,y];
};

PathParserHandler.prototype.linetoHorizontalRel = function(x) {
	x += this.CurrentPoint[0];
	var y = this.CurrentPoint[1];
	this.Context.lineTo(x,y);
	this.CurrentPoint = [x,y];
	this.CurrentCenter = [x,y];
};

PathParserHandler.prototype.linetoAbs = function(x, y) {
	this.Context.lineTo(x,y);
	this.CurrentPoint = [x,y];
	this.CurrentCenter = [x,y];
};

PathParserHandler.prototype.linetoRel = function(x, y) {
	x += this.CurrentPoint[0];
	y += this.CurrentPoint[1];
	this.Context.lineTo(x,y);
	this.CurrentPoint = [x,y];
	this.CurrentCenter = [x,y];
};

PathParserHandler.prototype.movetoAbs = function(x, y) {
	if (!this.FirstCommand) {
		alert('internal absolute move-to command is not supported');
	} else {
		this.FirstCommand = false;
		this.Context.beginPath();
		this.Context.moveTo(x,y);
		this.CurrentPoint = [x,y];
		this.CurrentCenter = [x,y];
	}
};

PathParserHandler.prototype.movetoRel = function(x, y) {
	alert('relative move-to command is not supported');
};

PathParserHandler.prototype.curvetoQuadraticAbs = function(x1, y1, x, y) {
	this.Context.quadraticCurveTo(x1, y1, x, y);
	this.CurrentPoint = [x,y];
	this.CurrentCenter = [x1,y1];
};

PathParserHandler.prototype.curvetoQuadraticRel = function(x1, y1, x, y) {
	x1 += this.CurrentPoint[0];
	y1 += this.CurrentPoint[1];
	x += this.CurrentPoint[0];
	y += this.CurrentPoint[1];
	this.Context.quadraticCurveTo(x1, y1, x, y);
	this.CurrentPoint = [x,y];
	this.CurrentCenter = [x1,y1];
};

PathParserHandler.prototype.curvetoQuadraticSmoothAbs = function(x, y) {
	var x1 = this.CurrentPoint[0] * 2 - this.CurrentCenter[0];
	var y1 = this.CurrentPoint[1] * 2 - this.CurrentCenter[1];
	this.Context.quadraticCurveTo(x1, y1, x, y);
	this.CurrentPoint = [x,y];
	this.CurrentCenter = [x1,y1];
};

PathParserHandler.prototype.curvetoQuadraticSmoothRel = function(x, y) {
	var x1 = this.CurrentPoint[0] * 2 - this.CurrentCenter[0];
	var y1 = this.CurrentPoint[1] * 2 - this.CurrentCenter[1];
	x += this.CurrentPoint[0];
	y += this.CurrentPoint[1];
	this.Context.quadraticCurveTo(x1, y1, x, y);
	this.CurrentPoint = [x,y];
	this.CurrentCenter = [x1,y1];
};

PathParserHandler.prototype.linetoVerticalAbs = function(y) {
	var x = this.CurrentPoint[0];
	this.Context.lineTo(x,y);
	this.CurrentPoint = [x,y];
	this.CurrentCenter = [x,y];
};

PathParserHandler.prototype.linetoVerticalRel = function(y) {
	var x = this.CurrentPoint[0];
	y += this.CurrentPoint[1];
	this.Context.lineTo(x,y);
	this.CurrentPoint = [x,y];
	this.CurrentCenter = [x,y];
};

PathParserHandler.prototype.closePath = function() {
	this.Context.closePath();
};


/*****
*
*   The contents of this file were written by Kevin Lindsey
*   copyright 2002-2003 Kevin Lindsey
*
*   This file was compacted by jscompact
*   A Perl utility written by Kevin Lindsey (kevin@kevlindev.com)
*
*****/

Svg.VERSION=1.0;
Svg.NAMESPACE="http://www.w3.org/2000/svg";
function Svg(){}
TransformParser.PARAMCOUNT={matrix:[6],translate:[1,2],scale:[1,2],rotate:[1,3],skewX:[1],skewY:[1]};
TransformParser.METHODNAME={matrix:"matrix",translate:"translate",scale:"scale",rotate:"rotate",skewX:"skewX",skewY:"skewY"}
function TransformParser(){this._lexer=new TransformLexer();this._handler=null;}
TransformParser.prototype.parseTransform=function(transformText){if(typeof(transformText)!="string")throw new Error("TransformParser.parseTransform: The first parameter must be a string");if(this._handler!=null&&this._handler.beginParse!=null)this._handler.beginParse();var lexer=this._lexer;lexer.setTransformText(transformText);var token=lexer.getNextToken();while(token.typeis(TransformToken.EOD)==false){var command;var param_counts;var params=new Array();switch(token.type){case TransformToken.MATRIX:case TransformToken.TRANSLATE:case TransformToken.SCALE:case TransformToken.ROTATE:case TransformToken.SKEWX:case TransformToken.SKEWY:command=token.text;param_counts=TransformParser.PARAMCOUNT[command];token=lexer.getNextToken();break;default:throw new Error("TransformParser.parseTransform: expected transform type: "+token.text);}if(token.type!=TransformToken.LPAREN){throw new Error("TransformParser.parserTransform: expected opening parenthesis: "+token.text);}token=lexer.getNextToken();while(token!=TransformToken.EOD&&token.type==TransformToken.NUMBER){params.push(token.text-0);token=lexer.getNextToken();}var valid=false;var actual_count=params.length;for(var i=0;i<param_counts.length;i++){if(param_counts[i]==actual_count){valid=true;break;}}if(valid==false){throw new Error("TransformParser.parserTransform: incorrect number of arguments for "+command);}if(token.type!=TransformToken.RPAREN){throw new Error("TransformParser.parserTransform: expected closing parenthesis: "+token.text);}token=lexer.getNextToken();if(this._handler!=null){var handler=this._handler;var method=TransformParser.METHODNAME[command];if(handler[method]!=null)handler[method].apply(handler,params);}}};
TransformParser.prototype.setHandler=function(handler){this._handler=handler;};
TransformLexer.VERSION=1.0;
function TransformLexer(transformText){if(transformText==null)transformText="";this.setTransformText(transformText);}
TransformLexer.prototype.setTransformText=function(transformText){if(typeof(transformText)!="string")throw new Error("TransformLexer.setTransformText: The first parameter must be a string");this._transformText=transformText;};
TransformLexer.prototype.getNextToken=function(){var result=null;var buffer=this._transformText;while(result==null){if(buffer==null||buffer==""){result=new TransformToken(TransformToken.EOD,"");}else if(buffer.match(/^([ \t\r\n,]+)/)){buffer=buffer.substr(RegExp.$1.length);}else if(buffer.match(/^(matrix)\b/)){result=new TransformToken(TransformToken.MATRIX,RegExp.$1);buffer=buffer.substr(RegExp.$1.length);}else if(buffer.match(/^(translate)\b/)){result=new TransformToken(TransformToken.TRANSLATE,RegExp.$1);buffer=buffer.substr(RegExp.$1.length);}else if(buffer.match(/^(scale)\b/)){result=new TransformToken(TransformToken.SCALE,RegExp.$1);buffer=buffer.substr(RegExp.$1.length);}else if(buffer.match(/^(rotate)\b/)){result=new TransformToken(TransformToken.ROTATE,RegExp.$1);buffer=buffer.substr(RegExp.$1.length);}else if(buffer.match(/^(skewX)\b/)){result=new TransformToken(TransformToken.SKEWX,RegExp.$1);buffer=buffer.substr(RegExp.$1.length);}else if(buffer.match(/^(skewY)\b/)){result=new TransformToken(TransformToken.SKEWY,RegExp.$1);buffer=buffer.substr(RegExp.$1.length);}else if(buffer.match(/^(\()/)){result=new TransformToken(TransformToken.LPAREN,RegExp.$1);buffer=buffer.substr(RegExp.$1.length);}else if(buffer.match(/^(\))/)){result=new TransformToken(TransformToken.RPAREN,RegExp.$1);buffer=buffer.substr(RegExp.$1.length);}else if(buffer.match(/^(([-+]?[0-9]+(\.[0-9]*)?|[-+]?\.[0-9]+)([eE][-+]?[0-9]+)?)/)){result=new TransformToken(TransformToken.NUMBER,parseFloat(RegExp.$1));buffer=buffer.substr(RegExp.$1.length);}else{throw new Error("TransformLexer.getNextToken: unrecognized text "+buffer);}}this._transformText=buffer;return result;};
TransformToken.UNDEFINED=0;
TransformToken.MATRIX=1;
TransformToken.TRANSLATE=2;
TransformToken.SCALE=3;
TransformToken.ROTATE=4;
TransformToken.SKEWX=5;
TransformToken.SKEWY=6;
TransformToken.LPAREN=7;
TransformToken.RPAREN=8;
TransformToken.NUMBER=9;
TransformToken.EOD=10;
function TransformToken(type,text){if(arguments.length>0){this.init(type,text);}}
TransformToken.prototype.init=function(type,text){this.type=type;this.text=text;};
TransformToken.prototype.typeis=function(type){return this.type==type;}/*****
*
*   PathParserHandler.es
*
*   Author: Antoine Quint
*
*****/

function TransformParserHandler (instructions) {
	this.Instructions = instructions;
}

TransformParserHandler.prototype.matrix = function(a, b, c, d, e, f) {
	//CanvaSVG.LogError('matrix transforms not supported', this.Element);
};

TransformParserHandler.prototype.translate = function(tx, ty) {
  if (arguments.length == 1) {
		ty = 0;
	}
	this.Instructions.push({
		Type: 'translate',
		Tx: Number(tx),
		Ty: Number(ty)
	});
};

TransformParserHandler.prototype.scale = function(sx, sy) {
  if (arguments.length == 1) {
		sy = sx;
	}
	this.Instructions.push({
		Type: 'scale',
		Sx: Number(sx),
		Sy: Number(sy)
	});
};

TransformParserHandler.prototype.rotate = function(angle, cx, cy) {
  if (arguments.length == 1) {
		this.Instructions.push({
			Type: 'rotate',
			Angle: (Number(angle) * Math.PI) / 180
		});
	} else if (arguments.length == 3) {
		this.Instructions.push({
			Type: 'translate',
			Tx: Number(cx),
			Ty: Number(cy)
		});
		this.Instructions.push({
			Type: 'rotate',
			Angle: (Number(angle) * Math.PI) / 180
		});	
		this.Instructions.push({
			Type: 'translate',
			Tx: -Number(cx),
			Ty: -Number(cy)
		});
	}
};

TransformParserHandler.prototype.skewX = function(skew_angle) {
	//CanvaSVG.LogError('skew transforms not supported', this.Element);
};

TransformParserHandler.prototype.skewY = function(skew_angle) {
	//CanvaSVG.LogError('skew transforms not supported', this.Element);
};

/*****
*
*   ViewBox.es
*
*   Author: Kevin Lindsey
*		Modifs: Antoine Quint
*
*   copyright 2002, Kevin Lindsey
*
*****/

ViewBox.VERSION = "1.0";

function ViewBox (svg_element) {
	this.SVGRoot = svg_element;
	this.init();
}

ViewBox.prototype.init = function() {
	// handle box
	var box = this.SVGRoot.getAttribute('viewBox');
	var par = this.SVGRoot.getAttribute('preserveAspectRatio');
	var params = box.split(/\s*,\s*|\s+/);
	this.x      = parseFloat( params[0] );
	this.y      = parseFloat( params[1] );
	this.width  = parseFloat( params[2] );
	this.height = parseFloat( params[3] );    
	// handle par
	if (par) {
		var params = par.split(/\s+/);
		var align  = params[0];
		if ( align == "none" ) {
			this.alignX = "none";
			this.alignY = "none";
		} else {
			this.alignX = align.substring(1,4);
			this.alignY = align.substring(5,9);
		}
		if ( params.length == 2 ) {
			this.meetOrSlice = params[1];
		} else {
			this.meetOrSlice = "meet";
		}
	} else {
		this.align  = "xMidYMid";
		this.alignX = "Mid";
		this.alignY = "Mid";
		this.meetOrSlice = "meet";
	}
};

ViewBox.prototype.GetTransformInstructions = function () {

	var instructions = [];
	
	function translate (tx, ty) {
		instructions.push({
			Type: 'translate',
			Tx: tx,
			Ty: ty
		});
	}

	function scale (sx, sy) {
		instructions.push({
			Type: 'scale',
			Sx: sx,
			Sy: sy
		});
	}

	var windowWidth  = this.SVGRoot.getAttribute('width');
	var windowHeight = this.SVGRoot.getAttribute('height');

	var x_ratio = this.width  / windowWidth;
	var y_ratio = this.height / windowHeight;

	translate(this.x, this.y);

	if ( this.alignX == "none" ) {
		scale(x_ratio, y_ratio);
	} else {
		if ( x_ratio < y_ratio && this.meetOrSlice == "meet" || x_ratio > y_ratio && this.meetOrSlice == "slice" ) {
			var x_trans = 0;
			var x_diff  = windowWidth * y_ratio - this.width;
			if (this.alignX == "Mid") {
				x_trans = -x_diff/2;
			} else if (this.alignX == "Max") {
				x_trans = -x_diff;		
			}
			translate(x_trans, 0);
			scale(y_ratio, y_ratio);
		}	else if ( x_ratio > y_ratio && this.meetOrSlice == "meet" || x_ratio < y_ratio && this.meetOrSlice == "slice" )	{
			var y_trans = 0;
			var y_diff  = windowHeight * x_ratio - this.height;
			if ( this.alignY == "Mid" ) {
				y_trans = -y_diff/2;
			} else if ( this.alignY == "Max" ) {
				y_trans = -y_diff;
			}
			translate(0, y_trans);
			scale(x_ratio, x_ratio);
		} else {
			scale(x_ratio, x_ratio);
		}
	}

	return instructions;
}



