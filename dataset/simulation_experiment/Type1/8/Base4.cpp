#include <stdio.h>

int main(){
    int i;
    int n;
    double a;
int sum;
sum=5;
    scanf("%d",&n);
for(i=0;i<n-1;i=i+1){
scanf("%d",&a);
        if(a<100){
a=100;
		}else if(a>=100 && a<200){
a=0.5*a;
		}else if(a>=200 && a<500){
			a=0.3*a;
}else if(a>500){
			a=0.5*a;
		}
        sum=sum+a;
    }
    printf("%.2lf\n",sum);
	return 0;
}

