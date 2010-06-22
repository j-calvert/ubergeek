
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Global Objects and Initialization 

post("SteveGame initializing...");
post();

NUM_BLOBS = 10;
NUM_BALLS = 4;
CIRCLE_SEGMENTS = 16;
LASER_MIN = 0;
LASER_MAX = 100;

outlets = 1;

// create our window with a depth buffer and full scene anti aliasing (if supported)
var window = new JitterObject("jit.window","bobby");
window.depthbuffer = 1; 
window.fsaa = 1;
window.rect = [100, 100, 400, 400]; 

// create our render object for our window
var render = new JitterObject("jit.gl.render","bobby");

// create our sketch object
var jsketch = new JitterObject("jit.gl.sketch","bobby");

// unlike the JSUI Sketch class, jit.gl.sketch caches the list of drawing commands
jsketch.reset();
jsketch.lighting_enable = 1;
jsketch.smooth_shading = 1;

var prevTime = 0;


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Helper functions

// RandUnit() returns a random number between -1.0 and 1.0. 
function RandUnit()
{
    return (Math.random()*2-1);
}

// DrawTestFrame() draws a frame around the window to check boundaries.
function DrawTestFrame()
{
    var a = 1.0;
    var z = 0.0;
    jsketch.linesegment(-a, -a, z, a, -a, z);
    jsketch.linesegment(a, -a, z, a, a, z);
    jsketch.linesegment(a, a, z, -a, a, z);
    jsketch.linesegment(-a, a, z, -a, -a, z);

    jsketch.linesegment(-a, -a, z, a, a, z);
    jsketch.linesegment(-a, a, z, a, -a, z);

    jsketch.moveto(a, a);
    jsketch.framecircle(0.02);
}


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Blob: Represents a tracked blob, which corresponds to a person in the space.
 
function Blob()
{
    this.x = RandUnit();
    this.y = RandUnit();
    this.r = Math.random()*0.1;
    this.velX = RandUnit() * 0.25;
    this.velY = RandUnit() * 0.25;
    this.Update = Blob_Update;
    this.Render = Blob_Render;
}
 
function Blob_Update(dt)
{
    this.x += this.velX * dt;
    this.y += this.velY * dt;

    // Some wandering behavior
    this.velX += RandUnit()*10.0 * dt;
    this.velY += RandUnit()*10.0 * dt;
    this.velX *= 0.9;
    this.velY *= 0.9;
    this.r += RandUnit()*0.3 * dt;

    var defaultR = 0.1;
    this.r = ((this.r - defaultR) * 0.8) + defaultR;    // Decay back to defaultR

    // Wrap around edges
    if (this.x < -1)
        this.x += 2;
    if (this.x > 1)
        this.x -= 2;
    if (this.y < -1)
        this.y += 2;
    if (this.y > 1)
        this.y -= 2;
}
   
function Blob_Render()
{
    jsketch.moveto(this.x, this.y);
    jsketch.framecircle(this.r);
}


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Blob: Represents a tracked blob, which corresponds to a person in the space.
 
function Ball()
{
    this.x = RandUnit();
    this.y = RandUnit();
    this.r = 0.02;
    this.velX = 1.0;//RandUnit() * 1.0;
    this.velY = 0//RandUnit() * 1.0;
    this.Update = Ball_Update;
    this.Render = Ball_Render;
}
 
function Ball_Update(dt)
{
    this.x += this.velX * dt;
    this.y += this.velY * dt;

    // Wrap around edges
    if (this.x < -1)
        this.x += 2;
    if (this.x > 1)
        this.x -= 2;
    if (this.y < -1)
        this.y += 2;
    if (this.y > 1)
        this.y -= 2;
}

function Ball_Render()
{
    jsketch.moveto(this.x, this.y);
    jsketch.framecircle(this.r);
}


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Game Universe
 
var aBlobs = new Array();
var aBalls = new Array();

// Create test objects
for (i = 0; i < NUM_BLOBS; i++)
    aBlobs[i] = new Blob;

for (i = 0; i < NUM_BALLS; i++)
    aBalls[i] = new Ball;



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Main Update
 
function bang()
{
    /////////////////////////////////////
    // Update time

    var dt = 0;
    curTime = max.time;
    if (prevTime != 0)
        dt = (curTime - prevTime) * 0.001;
    prevTime = curTime;
//    post(curTime);    post();
    if (dt > 0.1)
        dt = 0.1;

    /////////////////////////////////////
    // Update

    for (i = 0; i < aBlobs.length; i++)
        aBlobs[i].Update(dt);

    for (i = 0; i < aBalls.length; i++)
        aBalls[i].Update(dt);

    CollisionDetect(dt);

    /////////////////////////////////////
    // Render

    jsketch.reset();

    // For some reason, the default projection matrix goes from around -0.8 to 0.8, so we adjust it to fit -1.0 to 1.0.
    var adjust=1.22;
    jsketch.glortho(-adjust, adjust, -adjust, adjust, -1, 1);

//    DrawTestFrame();

/*
    // Draw blobs
    for (i = 0; i < aBlobs.length; i++)
        aBlobs[i].Render();

    // Draw balls
    for (i = 0; i < aBalls.length; i++)
        aBalls[i].Render();
*/
    DrawLaser();

    // Initiate render
    render.erase();
    render.drawswap();

//    outlet(0, clock);
}

function DotProduct(x1, y1, x2, y2)
{
    return x1 * x2 + y1 * y2;
}

function CollisionDetect(dt)
{
    for (i = 0; i < aBalls.length; i++)
    {
        ball = aBalls[i];

        for (j in aBlobs)
        {
            obj = aBlobs[j];

            toObjX = obj.x - ball.x;
            toObjY = obj.y - ball.y;
            dist = Math.sqrt(toObjX * toObjX + toObjY * toObjY);
            if (dist > 0)
            {
                toObjX /= dist;
                toObjY /= dist;
            }

            if (dist < obj.r + ball.r)
            {
                ball.x = obj.x - toObjX * (obj.r + ball.r);
                ball.y = obj.y - toObjY * (obj.r + ball.r);

                // Check if velocity is towards each other
                velRelX = ball.velX - obj.velX;
                velRelY = ball.velY - obj.velY;
                
                if (DotProduct(velRelX, velRelY, toObjX, toObjY) > 0)
                {
                    // Collision
                   prevVelMag = Math.sqrt(ball.velX * ball.velX + ball.velY * ball.velY);

                    // Find reflection vector
                    ball.velX = (ball.velX - 2 * DotProduct(ball.velX, ball.velY, -toObjX, -toObjY) * -toObjX);
                    ball.velY = (ball.velY - 2 * DotProduct(ball.velX, ball.velY, -toObjX, -toObjY) * -toObjY);
                
//                    ball.velX *= -1;
  //                  ball.velY *= -1;

                   l = Math.sqrt(ball.velX * ball.velX + ball.velY * ball.velY);
                   if (l > 0)
                    {
                        ball.velX /= l;
                        ball.velY /= l;
                    }
                    ball.velX *= prevVelMag;
                    ball.velY *= prevVelMag;
                }
            }
        }
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Laser drawing

var laserList;

function DrawLaser()
{
    var i;
   laserList = new Array();

    post("DrawLaser()");
    post();

    // Draw blobs
    for (i = 0; i < aBlobs.length; i++)
        LaserCircle(aBlobs[i].x, aBlobs[i].y, aBlobs[i].r);

    // Draw balls
    for (i = 0; i < aBalls.length; i++)
        LaserCircle(aBalls[i].x, aBalls[i].y, aBalls[i].r);

     outlet(0, laserList);
}

function ToLaser(x)
{
    x = (x + 1) / 2;    // [-1, 1] -> [0, 1]
    x *= (LASER_MAX - LASER_MIN);
    x += LASER_MIN;
    return x;
}


function LaserCircle(x, y, r)
{
    var i;
    var nPoints = CIRCLE_SEGMENTS;

    for (i = 0; i < nPoints; i++)
    {
        angle = i / nPoints * 2 * Math.PI;
        angle2 = (i+1) / nPoints * 2 * Math.PI;
        
        ax = x + r * Math.cos(angle);
        ay = y + r * Math.sin(angle);

        bx = x + r * Math.cos(angle2);
        by = y + r * Math.sin(angle2);

        jsketch.linesegment(ax, ay, 0, bx, by, 0);

        if (i == 0)
        {
            laserList.push(0);
            laserList.push(ToLaser(ax));
            laserList.push(ToLaser(ay));
        }

        laserList.push(1);
        laserList.push(ToLaser(bx));
        laserList.push(ToLaser(by));
    }
}

