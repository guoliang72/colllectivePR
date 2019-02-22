#include<stdio.h>
int main()
{
	int n,i,j,k;
	int a[1000],t;
	scanf("%d",&n);
	for(i=0;i<n;i=i+1)
	{
		scanf("%d",&a[i]);
	}
	scanf("%d",&k);
	for(i=0;i<n;i=i+1)
	{
		for(j=0;j<n-i-1;j=j+1)
		{
			if(a[j]<a[j+1])
			{
				t=a[j+1];
				a[j+1]=a[j];
				a[j]=t;
			}
		}
	}
	for(i=0;i<n-k+1;i=i+1)			// 输出循环边界错误
	{
		printf("%d\n",a[i]);
	}
	return 0;
}