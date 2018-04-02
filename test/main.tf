provider "aws" {
  region  = "us-east-1"
  version = "1.7.1"
}

provider "template" {
  version = "1.0"
}

module "vpc" {
   source  = "npalm/vpc/aws"
  version = "1.1.0"

  environment = "blog"
  aws_region  = "us-east-1"

  // optional, defaults
  create_private_hosted_zone = "false"

  // example to override default availability_zones
  availability_zones = {
    us-east-1 = ["us-east-1a", "us-east-1b", "us-east-1c"]
  }
  
}

resource "aws_ecs_cluster" "cluster" {
  name = "blog-ecs-cluster"
}

resource "aws_cloudwatch_log_group" "log_group" {
  name = "blog"
}

##task for ECS

data "aws_iam_policy_document" "ecs_tasks_execution_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "ecs_tasks_execution_role" {
  name               = "blog-ecs-task-execution-role"
  assume_role_policy = "${data.aws_iam_policy_document.ecs_tasks_execution_role.json}"
}

resource "aws_iam_role_policy_attachment" "ecs_tasks_execution_role" {
  role       = "${aws_iam_role.ecs_tasks_execution_role.name}"
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

data "template_file" "blog" {
  template = <<EOF
  [
    {
      "essential": true,
      "image": "364833262723.dkr.ecr.us-east-1.amazonaws.com/amq2:latest",
      "name": "blog",
      "environment": [{
      "name": "broker.url",
      "value": "tcp://ec2-54-211-227-182.compute-1.amazonaws.com:61616"
    },
    {
      "name": "SECRET",
      "value": "KEY"
    }],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "blog",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]

  EOF
}

resource "aws_ecs_task_definition" "task" {
  family                   = "blog-blog"
  container_definitions    = "${data.template_file.blog.rendered}"
  network_mode             = "awsvpc"
  cpu                      = "256"
  memory                   = "512"
  requires_compatibilities = ["FARGATE"]
  execution_role_arn       = "${aws_iam_role.ecs_tasks_execution_role.arn}"
}

##ECS service

resource "aws_security_group" "awsvpc_sg" {
  name   = "blog-awsvpc-cluster-sg"
  vpc_id = "${module.vpc.vpc_id}"

  ingress {
    protocol  = "tcp"
    from_port = 0
    to_port   = 65535

    cidr_blocks = [
      "${module.vpc.vpc_cidr}",
    ]
  }

  egress {
    from_port   = 0
    to_port     = 65535
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags {
    Name        = "blog-ecs-cluster-sg"
    Environment = "blog"
  }
}
# Simply specify the family to find the latest ACTIVE revision in that family.
#data "aws_ecs_task_definition" "task" {
# task_definition = "${aws_ecs_task_definition.task.family}"
#}

resource "aws_ecs_service" "service" {
  name            = "blog"
  cluster         = "${aws_ecs_cluster.cluster.id}"
  task_definition = "${aws_ecs_task_definition.task.arn}"
  desired_count   = 1
  # Track the latest ACTIVE revision
  #task_definition = "${aws_ecs_task_definition.task.family}:${max("${aws_ecs_task_definition.task.revision}", "${data.aws_ecs_task_definition.task.revision}")}"

  launch_type = "FARGATE"

  network_configuration {
    security_groups = ["${aws_security_group.awsvpc_sg.id}"]
    subnets         = ["${module.vpc.private_subnets}"]
  }

  
}




